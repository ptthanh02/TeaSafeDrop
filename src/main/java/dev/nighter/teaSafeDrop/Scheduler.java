package dev.nighter.teaSafeDrop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Universal scheduler utility that supports both traditional Bukkit scheduling
 * and Folia's region-based scheduling system.
 *
 * This class automatically detects which server implementation is being used
 * and provides appropriate scheduling methods.
 */
public final class Scheduler {

    private static final Plugin plugin;
    private static final boolean isFolia;

    static {
        plugin = TeaSafeDrop.getInstance();

        // Check if we're running on Folia
        boolean foliaDetected = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            foliaDetected = true;
            plugin.getLogger().info("Folia detected! Using region-based threading system.");
        } catch (final ClassNotFoundException e) {
            plugin.getLogger().info("Running on standard Bukkit/Spigot/Paper server.");
        }
        isFolia = foliaDetected;
    }

    /**
     * Runs a task on the main thread (or global region in Folia).
     *
     * @param runnable The task to run
     * @return A Task object representing the scheduled task
     */
    public static Task runTask(Runnable runnable) {
        if (isFolia) {
            try {
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> runnable.run());
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error scheduling task in Folia", e);
                return new Task(null);
            }
        } else {
            return new Task(Bukkit.getScheduler().runTask(plugin, runnable));
        }
    }

    /**
     * Runs a task asynchronously.
     *
     * @param runnable The task to run
     * @return A Task object representing the scheduled task
     */
    public static Task runTaskAsync(Runnable runnable) {
        if (isFolia) {
            try {
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> runnable.run());
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error scheduling async task in Folia", e);
                return new Task(null);
            }
        } else {
            return new Task(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
        }
    }

    /**
     * Runs a task after a specified delay.
     *
     * @param runnable   The task to run
     * @param delayTicks The delay in ticks before running the task
     * @return A Task object representing the scheduled task
     */
    public static Task runTaskLater(Runnable runnable, long delayTicks) {
        if (isFolia) {
            try {
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> runnable.run(),
                                delayTicks < 1 ? 1 : delayTicks);
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error scheduling delayed task in Folia", e);
                return new Task(null);
            }
        } else {
            return new Task(Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks));
        }
    }

    /**
     * Runs a task asynchronously after a specified delay.
     *
     * @param runnable   The task to run
     * @param delayTicks The delay in ticks before running the task
     * @return A Task object representing the scheduled task
     */
    public static Task runTaskLaterAsync(Runnable runnable, long delayTicks) {
        if (isFolia) {
            try {
                long delayMs = delayTicks * 50; // Convert ticks to milliseconds
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> runnable.run(),
                                delayMs, TimeUnit.MILLISECONDS);
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error scheduling delayed async task in Folia", e);
                return new Task(null);
            }
        } else {
            return new Task(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks));
        }
    }

    /**
     * Runs a task repeatedly at fixed intervals.
     *
     * @param runnable    The task to run
     * @param delayTicks  The initial delay in ticks before the first execution
     * @param periodTicks The period in ticks between subsequent executions
     * @return A Task object representing the scheduled task
     */
    public static Task runTaskTimer(Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia) {
            try {
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> runnable.run(),
                                delayTicks < 1 ? 1 : delayTicks, periodTicks);
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error scheduling timer task in Folia", e);
                return new Task(null);
            }
        } else {
            return new Task(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks));
        }
    }

    /**
     * Runs a task repeatedly at fixed intervals asynchronously.
     *
     * @param runnable    The task to run
     * @param delayTicks  The initial delay in ticks before the first execution
     * @param periodTicks The period in ticks between subsequent executions
     * @return A Task object representing the scheduled task
     */
    public static Task runTaskTimerAsync(Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia) {
            try {
                // Convert ticks to milliseconds (1 tick = 50ms)
                long delayMs = delayTicks * 50;
                long periodMs = periodTicks * 50;

                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> runnable.run(),
                                delayMs, periodMs, TimeUnit.MILLISECONDS);
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error scheduling timer async task in Folia", e);
                return new Task(null);
            }
        } else {
            return new Task(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks));
        }
    }

    /**
     * Runs a task in the region of a specific entity.
     * Falls back to regular scheduling on non-Folia servers.
     *
     * @param entity   The entity in whose region to run the task
     * @param runnable The task to run
     * @return A Task object representing the scheduled task
     */
    public static Task runEntityTask(Entity entity, Runnable runnable) {
        if (isFolia && entity != null) {
            try {
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        entity.getScheduler().run(plugin, scheduledTask -> runnable.run(), null);
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error scheduling entity task in Folia, falling back to global scheduler", e);
                return runTask(runnable);
            }
        } else {
            return runTask(runnable);
        }
    }

    /**
     * Runs a delayed task in the region of a specific entity.
     *
     * @param entity     The entity in whose region to run the task
     * @param runnable   The task to run
     * @param delayTicks The delay in ticks before running the task
     * @return A Task object representing the scheduled task
     */
    public static Task runEntityTaskLater(Entity entity, Runnable runnable, long delayTicks) {
        if (isFolia && entity != null) {
            try {
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        entity.getScheduler().runDelayed(plugin, scheduledTask -> runnable.run(), null,
                                delayTicks < 1 ? 1 : delayTicks);
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error scheduling delayed entity task in Folia, falling back to global scheduler", e);
                return runTaskLater(runnable, delayTicks);
            }
        } else {
            return runTaskLater(runnable, delayTicks);
        }
    }

    /**
     * Runs a repeated task in the region of a specific entity.
     *
     * @param entity     The entity in whose region to run the task
     * @param runnable   The task to run
     * @param delayTicks The initial delay in ticks before the first execution
     * @param periodTicks The period in ticks between subsequent executions
     * @return A Task object representing the scheduled task
     */
    public static Task runEntityTaskTimer(Entity entity, Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia && entity != null) {
            try {
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        entity.getScheduler().runAtFixedRate(plugin, scheduledTask -> runnable.run(), null,
                                delayTicks < 1 ? 1 : delayTicks, periodTicks);
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error scheduling timer entity task in Folia, falling back to global scheduler", e);
                return runTaskTimer(runnable, delayTicks, periodTicks);
            }
        } else {
            return runTaskTimer(runnable, delayTicks, periodTicks);
        }
    }

    /**
     * Runs a task in the region of a specific location.
     * Falls back to regular scheduling on non-Folia servers.
     *
     * @param location The location in whose region to run the task
     * @param runnable The task to run
     * @return A Task object representing the scheduled task
     */
    public static Task runLocationTask(Location location, Runnable runnable) {
        if (isFolia && location != null && location.getWorld() != null) {
            try {
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> runnable.run());
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error scheduling location task in Folia, falling back to global scheduler", e);
                return runTask(runnable);
            }
        } else {
            return runTask(runnable);
        }
    }

    /**
     * Runs a delayed task in the region of a specific location.
     *
     * @param location   The location in whose region to run the task
     * @param runnable   The task to run
     * @param delayTicks The delay in ticks before running the task
     * @return A Task object representing the scheduled task
     */
    public static Task runLocationTaskLater(Location location, Runnable runnable, long delayTicks) {
        if (isFolia && location != null && location.getWorld() != null) {
            try {
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> runnable.run(),
                                delayTicks < 1 ? 1 : delayTicks);
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error scheduling delayed location task in Folia, falling back to global scheduler", e);
                return runTaskLater(runnable, delayTicks);
            }
        } else {
            return runTaskLater(runnable, delayTicks);
        }
    }

    /**
     * Runs a repeated task in the region of a specific location.
     *
     * @param location   The location in whose region to run the task
     * @param runnable   The task to run
     * @param delayTicks The initial delay in ticks before the first execution
     * @param periodTicks The period in ticks between subsequent executions
     * @return A Task object representing the scheduled task
     */
    public static Task runLocationTaskTimer(Location location, Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia && location != null && location.getWorld() != null) {
            try {
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, scheduledTask -> runnable.run(),
                                delayTicks < 1 ? 1 : delayTicks, periodTicks);
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error scheduling timer location task in Folia, falling back to global scheduler", e);
                return runTaskTimer(runnable, delayTicks, periodTicks);
            }
        } else {
            return runTaskTimer(runnable, delayTicks, periodTicks);
        }
    }

    /**
     * Runs a task in the region of a specific location in a world.
     * Falls back to regular scheduling on non-Folia servers.
     *
     * @param location  The location in whose region to run the task
     * @param runnable  The task to run
     * @return A Task object representing the scheduled task
     */
    public static Task runWorldTask(Location location, Runnable runnable) {
        if (isFolia && location != null && location.getWorld() != null) {
            try {
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> runnable.run());
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error scheduling world task in Folia, falling back to global scheduler", e);
                return runTask(runnable);
            }
        } else {
            return runTask(runnable);
        }
    }

    /**
     * Runs a delayed task in the region of a specific location in a world.
     *
     * @param location   The location in whose region to run the task
     * @param runnable   The task to run
     * @param delayTicks The delay in ticks before running the task
     * @return A Task object representing the scheduled task
     */
    public static Task runWorldTaskLater(Location location, Runnable runnable, long delayTicks) {
        if (isFolia && location != null && location.getWorld() != null) {
            try {
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> runnable.run(),
                                delayTicks < 1 ? 1 : delayTicks);
                return new Task(task);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error scheduling delayed world task in Folia, falling back to global scheduler", e);
                return runTaskLater(runnable, delayTicks);
            }
        } else {
            return runTaskLater(runnable, delayTicks);
        }
    }

    /**
     * Creates a CompletableFuture that will be completed on the main thread or global region.
     *
     * @param <T>      The type of the result
     * @param supplier The supplier providing the result
     * @return A CompletableFuture that will be completed with the result
     */
    public static <T> CompletableFuture<T> supplySync(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();

        try {
            if (isFolia) {
                Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
                    try {
                        future.complete(supplier.get());
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                        plugin.getLogger().log(Level.SEVERE, "Error while executing sync task", t);
                    }
                });
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        future.complete(supplier.get());
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                        plugin.getLogger().log(Level.SEVERE, "Error while executing sync task", t);
                    }
                });
            }
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }

        return future;
    }

    /**
     * Creates a CompletableFuture that will be completed asynchronously.
     *
     * @param <T>      The type of the result
     * @param supplier The supplier providing the result
     * @return A CompletableFuture that will be completed with the result
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();

        try {
            if (isFolia) {
                Bukkit.getAsyncScheduler().runNow(plugin, task -> {
                    try {
                        future.complete(supplier.get());
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                        plugin.getLogger().log(Level.SEVERE, "Error while executing async task", t);
                    }
                });
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        future.complete(supplier.get());
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                        plugin.getLogger().log(Level.SEVERE, "Error while executing async task", t);
                    }
                });
            }
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }

        return future;
    }

    /**
     * Determines if the server is running on Folia.
     *
     * @return true if the server is running on Folia
     */
    public static boolean isFolia() {
        return isFolia;
    }

    /**
     * Wrapper class for both Bukkit and Folia tasks.
     */
    public static class Task {
        private final Object task;

        /**
         * Creates a new Task.
         *
         * @param task The underlying task object
         */
        Task(Object task) {
            this.task = task;
        }

        /**
         * Cancels the task.
         */
        public void cancel() {
            if (task == null) {
                return;
            }

            try {
                if (isFolia) {
                    if (task instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask) {
                        ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).cancel();
                    }
                } else {
                    if (task instanceof BukkitTask) {
                        ((BukkitTask) task).cancel();
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to cancel task", e);
            }
        }

        /**
         * Gets the underlying task object.
         *
         * @return The underlying task object
         */
        public Object getTask() {
            return task;
        }

        /**
         * Checks if this task is cancelled.
         *
         * @return true if the task is cancelled
         */
        public boolean isCancelled() {
            if (task == null) {
                return true;
            }

            try {
                if (isFolia) {
                    if (task instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask) {
                        return ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).isCancelled();
                    }
                } else {
                    if (task instanceof BukkitTask) {
                        return ((BukkitTask) task).isCancelled();
                    }
                }
            } catch (Exception ignored) {
                // Task may have already been garbage collected or is invalid
            }

            return true;
        }
    }
}