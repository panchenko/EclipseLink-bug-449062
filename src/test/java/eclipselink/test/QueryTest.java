package eclipselink.test;

import eclipselink.domain.Foo;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import org.eclipse.persistence.config.PessimisticLock;
import org.eclipse.persistence.config.QueryHints;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueryTest extends Assert {

    private static EntityManagerFactory entityManagerFactory;

    @BeforeClass
    public static void managerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("domain");
    }

    private EntityManager em;

    @Before
    public void entityManager() {
        em = entityManagerFactory.createEntityManager();
    }

    private void inTransaction(Runnable runnable) {
        final EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            runnable.run();
        } finally {
            try {
                tx.rollback();
            } catch (IllegalStateException | PersistenceException rollbackError) {
                System.err.println("Rollback error");
                rollbackError.printStackTrace();
            }
        }
    }

    @Test
    public void limitWithoutForUpdate() {
        inTransaction(new Runnable() {
            public void run() {
                insertData();
                final List<Foo> data = em.createQuery("select f from Foo f", Foo.class).setMaxResults(1).getResultList();
                assertEquals(1, data.size());
            }
        });
    }

    @Test
    public void limitWithForUpdate() {
        inTransaction(new Runnable() {
            public void run() {
                insertData();
                final List<Foo> data = em.createQuery("select f from Foo f", Foo.class).setMaxResults(1).setHint(QueryHints.PESSIMISTIC_LOCK, PessimisticLock.Lock).getResultList();
                assertEquals(1, data.size());
            }
        });
    }

    @Test
    public void forUpdate() {
        inTransaction(new Runnable() {
            public void run() {
                insertData();
                final List<Foo> data = em.createQuery("select f from Foo f", Foo.class).setHint(QueryHints.PESSIMISTIC_LOCK, PessimisticLock.Lock).getResultList();
                assertEquals(2, data.size());
            }
        });
    }

    private void insertData() {
        for (String name : new String[]{"A", "B"}) {
            final Foo foo = new Foo();
            foo.setName(name);
            em.persist(foo);
        }
        em.flush();
    }
}
