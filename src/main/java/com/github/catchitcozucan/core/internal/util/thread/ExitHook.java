package com.github.catchitcozucan.core.internal.util.thread;


public class ExitHook extends Thread {
	Exitable toTerminate;

	public ExitHook() {}

	public ExitHook(Thread t) {
		if (t instanceof Exitable) {
			this.toTerminate = (Exitable) t;
		}
	}

	public ExitHook(Exitable t) {
		this.toTerminate = t;
	}

	@Override
	public void run() {
		this.toTerminate.exitz();
	}

	public static void addExitHook(Thread t) {
		if (t instanceof Exitable) {
			ExitHook hook = new ExitHook(t);
			Runtime.getRuntime().addShutdownHook(hook);
		} else {
			throw new IllegalArgumentException("Thread does not implement the Exitable interface!");
		}
	}

	public static void addExitHook(Exitable t) {
		ExitHook hook = new ExitHook(t);
		Runtime.getRuntime().addShutdownHook(hook);
	}

	public static void add(final Object instance) { // NOSONAR
		if (!(instance instanceof Thread) && !(instance instanceof Exitable)) {
			throw new IllegalArgumentException("Theead or Exiatable are permitted!");
		} else {
			Thread hook = new Thread(() -> {
				if (instance != null) {
					if (instance instanceof Exitable) {
						((Exitable) instance).exitz();
					} else {
						try {
							((Thread) instance).join();
						} catch (InterruptedException ignore) {
						} // NOSONAR this is the exit hook, we do NOT care.
						try {
							Thread.sleep(50L);
						} catch (InterruptedException ignore) {
						}  // NOSONAR this is the exit hook, we do NOT care.
						((Thread) instance).interrupt();
					}
				}

			});
			Runtime.getRuntime().addShutdownHook(hook);
		}
	}
}

