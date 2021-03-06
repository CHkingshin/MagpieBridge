package cn.chihsien.HTTPS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @describe
 * @auther chihsiencheng
 */
public class KillDemo {
    private Integer stock = 6;//数据库6个库存

    private BlockingQueue<RequestPromise> queue = new LinkedBlockingQueue<>(9);

    /***
     * @describe
     * 启动10个线程 库存6个 生成一个合并队列 每个用户能拿到自己的请求响应
     */
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        KillDemo killDemo = new KillDemo();
        killDemo.mergeJob();
        Thread.sleep(2000);

        List<Future<Result>> futureList = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            final Long orderId = i + 100L;
            final Long userId = Long.valueOf(i);
            Future<Result> future = executorService.submit(() -> {
                countDownLatch.countDown();
                countDownLatch.await(1000,TimeUnit.SECONDS);
                return killDemo.operate(new UserRequest(orderId, userId, 1));
            });
            futureList.add(future);
        }

       /* futureList.forEach(future -> {
            try {
                Result result = future.get(300, TimeUnit.MILLISECONDS);
                System.out.println(Thread.currentThread().getName() + ":客户端请求响应:" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });*/
        //换为completableFuture 哪个结果先到先处理哪个
        futureList.forEach(completableFuture -> {
            try {
                Result result = completableFuture.get(300, TimeUnit.MILLISECONDS);
                System.out.println(Thread.currentThread().getName() + ":客户端请求响应:" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 用户扣除库存的方法
     */
    public Result operate(UserRequest userRequest) throws InterruptedException {
        // TODO 阈值判断
        // TODO 队列的创建
        RequestPromise requestPromise = new RequestPromise(userRequest);
        synchronized (requestPromise) {
            boolean enqueueSuccess = queue.offer(requestPromise, 100, TimeUnit.MILLISECONDS);
            if (! enqueueSuccess) {
                return new Result(false, "系统繁忙");
            }
            try {
                requestPromise.wait(200);
                //如果为空 则等待时间结束
                if (requestPromise.getResult() == null) {
                    return new Result(false, "等待超时");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                //return new Result(false, "被中断");
            }
        }
        return requestPromise.getResult();
    }
    public void mergeJob() {
        new Thread(() -> {
            List<RequestPromise> list = new ArrayList<>();
            while (true) {
                if (queue.isEmpty()) {
                    try {
                        Thread.sleep(10);
                        continue;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                /*TODO 从队列中取数据的时候加了batchSize，并且循环次数为batchSize 往list里加数据，
                   但是如果请求量不大的时候，队列里的数据数量小于batchSize，
                   这样list里会加了一些null，后面没有做判断
                */
                //获取队列长度 来判断取多少次 避免OOM
                int batchSize = queue.size();
                for (int i = 0; i < batchSize; i++) {
                    list.add(queue.poll());
                }

                System.out.println(Thread.currentThread().getName() + ":合并扣减库存:" + list);

                int sum = list.stream().mapToInt(e -> e.getUserRequest().getCount()).sum();
                // 两种情况
                if (sum <= stock) {
                    stock -= sum;
                    // notify user
                    list.forEach(requestPromise -> {
                        requestPromise.setResult(new Result(true, "ok"));
                        synchronized (requestPromise) {
                            requestPromise.notify();
                        }
                    });
                    continue;
                }
                for (RequestPromise requestPromise : list) {
                    int count = requestPromise.getUserRequest().getCount();
                    if (count <= stock) {
                        stock -= count;
                        requestPromise.setResult(new Result(true, "ok"));
                    } else {
                        requestPromise.setResult(new Result(false, "库存不足"));
                    }
                    synchronized (requestPromise) {
                        requestPromise.notify();
                    }
                }
                list.clear();
            }
        }, "mergeThread").start();
    }
}

