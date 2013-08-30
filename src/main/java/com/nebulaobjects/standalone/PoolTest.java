package com.nebulaobjects.standalone;

import java.util.Random;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class PoolTest {
  public static class TXClass {
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
      return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void doJob() {
      Session session = sessionFactory.getCurrentSession();
      // Just a stupid test to make sure everything is setup properly
      // session.createSQLQuery("select 1 from mysql.db").list();
      session.createSQLQuery("select benchmark(9999999, md5('when will it end?'));").list();
    }
  }

  public static void main(String args[]) throws Exception {
    ClassPathXmlApplicationContext ctx =
        new ClassPathXmlApplicationContext("/application-context.xml");
    System.in.read();
    for (int i = 0; i < 50; i++) {
      Thread t = new Thread(new Client(ctx));
      t.start();
      Thread.sleep(1000);
    }

    while (true) {
      Thread.sleep(Long.MAX_VALUE);
    }
  }

  static class Client implements Runnable {

    static Random rand = new Random();

    ClassPathXmlApplicationContext ctx;

    Client(ClassPathXmlApplicationContext ctx) {
      this.ctx = ctx;
    }

    public void run() {
      while (true) {
        TXClass tx = (TXClass) ctx.getBean("bean");
        tx.doJob();
        sleep();
        System.out.println(Thread.currentThread().getName() + " Job done");
      }
    }

    private void sleep() {
      int t = 1000 + Math.abs(rand.nextInt(9000));
      try {
        Thread.sleep(t);
      } catch (Exception e) {}
    }

  }

}
