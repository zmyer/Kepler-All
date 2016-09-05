package com.kepler.trace.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.kepler.admin.status.impl.StatusTask;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.impl.TraceContext;
import com.kepler.protocol.Request;
import com.kepler.service.Quiet;
import com.kepler.trace.TraceCause;
import com.kepler.trace.TraceCauses;

/**
 * @author KimShen
 *
 */
public class DefaultCauses implements TraceCauses {

	// Trace数量记录, 最多15条, 默认5条
	private static final int MAX = Math.max(Integer.valueOf(PropertiesUtils.get(DefaultCauses.class.getName().toLowerCase() + ".max", "5")), 15);

	private final AtomicInteger index = new AtomicInteger();

	private final Quiet quiet;

	/**
	 * 周期性缓存池 
	 */
	private TraceCause[] traces;

	public DefaultCauses(Quiet quiet) {
		super();
		this.reset();
		this.quiet = quiet;
	}

	/**
	 * 重置
	 */
	private void reset() {
		this.traces = new TraceCause[DefaultCauses.MAX + 1];
	}

	@Override
	public TraceCause[] get() {
		TraceCause[] traces = this.traces;
		this.reset();
		return traces;
	}

	@Override
	public void put(Request request, Throwable throwable) {
		// 收集非静默异常
		if (StatusTask.ENABLED && !this.quiet.quiet(request, throwable.getClass())) {
			this.traces[this.index.incrementAndGet() & DefaultCauses.MAX] = new DefaultCause(request.service(), request.method(), TraceContext.get());
		}
	}
}