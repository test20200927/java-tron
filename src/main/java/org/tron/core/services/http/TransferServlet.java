package org.tron.core.services.http;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Any;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.core.capsule.utils.TransactionUtil;
import org.tron.protos.Contract;
import org.tron.protos.Contract.TransferContract;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;

import static org.tron.core.services.http.Util.getVisible;
import static org.tron.core.services.http.Util.getVisiblePost;
import static org.tron.core.services.http.Util.setTransactionPermissionId;


@Component
@Slf4j(topic = "API")
public class TransferServlet extends HttpServlet {

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String contract = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(contract);
      boolean visible = getVisiblePost( contract );
      TransferContract.Builder build = TransferContract.newBuilder();
      JsonFormat.merge(contract, build, visible );
      JSONObject jsonObject = JSONObject.parseObject(contract);

      Transaction tx;
      long delaySeconds = 0;
      if (jsonObject.containsKey(Constant.DELAY_SECONDS)) {
        delaySeconds = jsonObject.getLong(Constant.DELAY_SECONDS);
      }
      if (delaySeconds > 0) {
        tx = wallet.createDeferredTransactionCapsule(build.build(), delaySeconds, ContractType.TransferContract).getInstance();
        tx = TransactionUtil.setTransactionDelaySeconds(tx, delaySeconds);
      } else {
        tx = wallet
            .createTransactionCapsule(build.build(), ContractType.TransferContract)
            .getInstance();
      }
      tx = setTransactionPermissionId(jsonObject, tx);
      response.getWriter().println(Util.printCreateTransaction(tx, visible));
    } catch (Exception e) {
      logger.debug("Exception: {}", e.getMessage());
      try {
        response.getWriter().println(Util.printErrorMsg(e));
      } catch (IOException ioe) {
        logger.debug("IOException: {}", ioe.getMessage());
      }
    }
  }
}
