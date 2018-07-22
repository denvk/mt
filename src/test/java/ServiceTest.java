
import com.denvk.mt.response.AccountException;
import com.denvk.mt.response.StatusCode;
import com.denvk.mt.service.Account;
import com.denvk.mt.service.AccountService;
import com.denvk.mt.service.InMemoryDatastore;
import com.denvk.mt.service.TransferDetails;
import java.math.BigDecimal;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Denis Voroshchuk
 */
public class ServiceTest {

    private final AccountService service = new AccountService(new InMemoryDatastore());

    public ServiceTest() {
    }

    @Test
    public void normalOperatingTest() {
        //normal operating
        System.out.println("Begin 2 accounts creation to test basic usage.");
        String id1 = "account1";
        BigDecimal value1 = BigDecimal.valueOf(100.100);
        Account created1 = service.create(id1, value1);
        Assert.assertNotEquals("Account creation fails.", null, created1);
        System.out.println(id1+" - account created");
        String id2 = "account2";
        BigDecimal value2 = BigDecimal.valueOf(100.100);
        Account created2 = service.create(id2, value2);
        Assert.assertNotEquals("Account creation fails.", null, created2);
        System.out.println(id2+" - account created");
        TransferDetails details = service.transfer(id1, id2, value1);
        Assert.assertNotEquals("Transfer fails.", null, details);
        System.out.println(value1+" was transfered from "+id1+" to "+id2+" successfully.");
        Account source = service.get(id1);
        Account target = service.get(id2);
        Assert.assertTrue("Transfer. Incorrect source rest.", BigDecimal.ZERO.compareTo(source.getAmount()) == 0);
        Assert.assertTrue("Transfer. Incorrect target rest.", value1.add(value2).compareTo(target.getAmount()) == 0);
    }

    @Test
    public void customExceptionsTest() {
        //reproduce predicted exceptions
        System.out.println("Begin series of test to check basic exceptions catching.");
        String id1 = "1";
        BigDecimal account1InitValue = BigDecimal.valueOf(100.100);
        Account created1 = service.create(id1, account1InitValue);
        Assert.assertNotEquals("Account creation fails.", null, created1);
        StatusCode expected = StatusCode.SAME_ACCOUNT_TRANSFER;
        try {
            service.transfer(id1, id1, BigDecimal.valueOf(50));
            Assert.fail("Incorrect behaviour. Transfer to the same account.");
        } catch (AccountException ae) {
            Assert.assertEquals("unpredicted exception occurs.", ae.getCode(), expected);
        }
        System.out.println(StatusCode.SAME_ACCOUNT_TRANSFER+" was catched.");
        expected = StatusCode.ALREADY_EXISTS;
        try {
            service.create(id1, account1InitValue);
            Assert.fail("Incorrect behaviour. Dublicate account was created.");
        } catch (AccountException ae) {
            Assert.assertEquals("unpredicted exception occurs.", ae.getCode(), expected);
        }
        System.out.println(StatusCode.ALREADY_EXISTS+" was catched.");
        expected = StatusCode.INVALID_ID;
        for (String mistakeId : new String[]{"", null}) {
            try {
                service.get(mistakeId);
                Assert.fail("Incorrect behaviour. None existed account was accessed.");
            } catch (AccountException ae) {
                Assert.assertEquals("unpredicted exception occurs.", ae.getCode(), expected);
            }
        }
        System.out.println(StatusCode.INVALID_ID+" was catched.");
        String id2 = "2";
        expected = StatusCode.INVALID_INIT_VALUE;
        try {
            service.create(id2, BigDecimal.valueOf(-1.0));
            Assert.fail("Incorrect behaviour. Create account with negative init value.");
        } catch (AccountException ae) {
            Assert.assertEquals("unpredicted exception occurs.", ae.getCode(), expected);
        }
        System.out.println(StatusCode.INVALID_INIT_VALUE+" was catched.");
        expected = StatusCode.NOT_FOUND;
        try {
            service.get("3");
            Assert.fail("Incorrect behaviour. None existed account was accessed.");
        } catch (AccountException ae) {
            Assert.assertEquals("unpredicted exception occurs.", ae.getCode(), expected);
        }
        System.out.println(StatusCode.NOT_FOUND+" was catched.");
        BigDecimal account2InitValue = BigDecimal.valueOf(100);
        Account created2 = service.create(id2, account2InitValue);
        Assert.assertNotEquals("Account creation fails.", null, created2);
        expected = StatusCode.INVALID_TRANSFER_VALUE;
        for (BigDecimal value : new BigDecimal[]{BigDecimal.ZERO, BigDecimal.valueOf(-1)}) {
            try {
                service.transfer(id1, id2, value);
                Assert.fail("Incorrect behaviour. Transfer zero value.");
            } catch (AccountException ae) {
                Assert.assertEquals("unpredicted exception occurs.", ae.getCode(), expected);
            }
        }
        System.out.println(StatusCode.INVALID_TRANSFER_VALUE+" was catched.");
        expected = StatusCode.NOT_ENOUGH_FUNDS;
        try {
            service.transfer(id1, id2, account1InitValue.add(BigDecimal.valueOf(0.000001)));
            Assert.fail("Incorrect behaviour. Transfer more than current value.");
        } catch (AccountException ae) {
            Assert.assertEquals("unpredicted exception occurs.", ae.getCode(), expected);
        }
        System.out.println(StatusCode.NOT_ENOUGH_FUNDS+" was catched.");
    }
    
}
