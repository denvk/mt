package com.denvk.mt.endpoint;

import com.denvk.mt.response.Response;
import com.denvk.mt.response.AccountException;
import com.denvk.mt.service.TransferDetails;
import com.denvk.mt.service.Account;
import com.denvk.mt.service.AccountService;
import com.denvk.mt.service.InMemoryDatastore;
import java.math.BigDecimal;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Denis Voroshchuk
 */
@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
public class AccountEndpoint {

    private static final Logger logger = LogManager.getLogger(AccountEndpoint.class);
    
    private static final AccountService service = 
            new AccountService(new InMemoryDatastore());

    @GET
    @Path("/create/{id}/{init}")
    public Response create(@PathParam("id") String id, @PathParam("init") BigDecimal initValue) {
        try {
            Account a = service.create(id, initValue);
            return new Response(a);
        } catch (AccountException e) {
            return new Response(e);
        } catch (Throwable t) {
            logger.error( String.format("Error occurs during create request id:%s,initValue:%s",id,initValue.toPlainString()),t);
            return Response.INTERNAL_SERVER_ERROR_RESPONSE;
        }
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        try {
            Account a = service.get(id);
            return new Response(a);
        } catch (AccountException e) {
            return new Response(e);
        } catch (Throwable t) {
            logger.error( String.format("Error occurs during get request id:%s",id),t);
            return Response.INTERNAL_SERVER_ERROR_RESPONSE;
        }
    }

    @GET
    @Path("/{source}/transfer/{target}/{value}")
    public Response transfer(@PathParam("source") String sourceId,
            @PathParam("target") String targetId,
            @PathParam("value") BigDecimal value) {
        try {
            TransferDetails td = service.transfer(sourceId, targetId, value);
            return new Response(td);
        } catch (AccountException e) {
            return new Response(e);
        } catch (Throwable t) {
            logger.error( String.format("Error occurs during transfer request sourceId:%s,targetId:%s,value:%s",sourceId,targetId,value.toPlainString()),t);
            return Response.INTERNAL_SERVER_ERROR_RESPONSE;
        }
    }
}
