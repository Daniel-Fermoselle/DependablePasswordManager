package pt.sec.a03.server.test;


import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractPasswordManagerTest {

	public AbstractPasswordManagerTest() {
		// TODO Auto-generated constructor stub
		
	}
	
	@BeforeClass
    public static void setUpBeforeAll() throws Exception {
    }

    @Before // run before each test
    public void setUp() throws Exception {
        try {
            populate();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After // rollback after each test
    public void tearDown() {
        try {        	
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void populate(); // each test adds its own data

}
