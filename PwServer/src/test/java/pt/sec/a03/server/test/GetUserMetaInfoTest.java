package pt.sec.a03.server.test;


import org.junit.Test;
import pt.sec.a03.server.database.Database;
import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.exception.InvalidArgumentException;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GetUserMetaInfoTest extends AbstractPasswordManagerTest {

    PasswordManager pwm;
    private String existingUserPK;
    private String newUserPK;
    private Database db;

    @Override
    protected void populate() {
        try{
            db = new Database();
            pwm = new PasswordManager();
            existingUserPK = "1234";
            newUserPK = "Bob";
            db.saveUser(existingUserPK);
            db.updateNonce(existingUserPK, 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void after() {
        try {
            db.runScript();
        } catch (FileNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test01_getUserMetaInfo(){
        assertTrue(pwm.validateNonceForUer(newUserPK, 0));
    }

    @Test
    public void test02_getUserMetaInfo(){
        try{
            pwm.getUserMetaInfo(null);
            fail("This test should fail with an InvalidArgumentException, null pointers were passed");
        }
        catch(InvalidArgumentException e){
        }
    }

    @Test
    public void test03_getUserMetaInfo(){
        assertTrue(pwm.validateNonceForUer(existingUserPK, 1));
    }

}
