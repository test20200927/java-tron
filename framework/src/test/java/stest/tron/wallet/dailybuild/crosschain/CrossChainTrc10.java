package stest.tron.wallet.dailybuild.crosschain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.PaginatedMessage;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.protos.Protocol.CrossMessage;
import org.tron.protos.Protocol.CrossMessage.Type;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.contract.BalanceContract;
import org.tron.protos.contract.BalanceContract.CrossContract.CrossDataType;
import stest.tron.wallet.common.client.utils.CrossChainBase;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class CrossChainTrc10 extends CrossChainBase {

  private Long sendAmount = 12L;

  @Test(enabled = true,description = "Transfer trc10 in cross chain")
  public void test01CreateCrossToken() throws InvalidProtocolBufferException {
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    final Long beforeFromTokenBalance = PublicMethed
        .getAssetBalanceByAssetId(assetAccountId1, trc10TokenAccountKey,blockingStubFull);
    final Long beforeToTokenBalance = PublicMethed
        .getAssetBalanceByAssetId(assetAccountId2, trc10TokenAccountKey,crossBlockingStubFull);
    final Long beforeFromBalance = PublicMethed
        .queryAccount(trc10TokenAccountAddress,blockingStubFull).getBalance();
    final Long beforeToBalance = PublicMethed
        .queryAccount(trc10TokenAccountAddress,crossBlockingStubFull).getBalance();

    final Long beforeBlockNum = blockingStubFull.getNowBlock(EmptyMessage.newBuilder()
        .build()).getBlockHeader().getRawData().getNumber();




    String txid = createCrossTrc10Transfer(trc10TokenAccountAddress,
        trc10TokenAccountAddress,assetAccountId1,6,sendAmount,name1,chainId,crossChainId,
        trc10TokenAccountKey,blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(crossBlockingStubFull);


    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    //Query first chain
    Optional<Transaction> byId = PublicMethed.getTransactionById(txid, blockingStubFull);
    Any any = byId.get().getRawData().getContract(0).getParameter();
    BalanceContract.CrossContract crossContract = any.unpack(BalanceContract.CrossContract.class);
    Assert.assertEquals(crossContract.getOwnerAddress(),
        ByteString.copyFrom(trc10TokenAccountAddress));
    Assert.assertEquals(crossContract.getToAddress(),
        ByteString.copyFrom(trc10TokenAccountAddress));
    Assert.assertEquals(crossContract.getOwnerChainId(),chainId);
    Assert.assertEquals(crossContract.getToChainId(),crossChainId);
    Assert.assertEquals(crossContract.getType(), CrossDataType.TOKEN);

    //Query second chain
    byId = PublicMethed.getTransactionById(txid, crossBlockingStubFull);
    any = byId.get().getRawData().getContract(0).getParameter();
    crossContract = any.unpack(BalanceContract.CrossContract.class);
    Assert.assertEquals(crossContract.getOwnerAddress(),
        ByteString.copyFrom(trc10TokenAccountAddress));
    Assert.assertEquals(crossContract.getToAddress(),
        ByteString.copyFrom(trc10TokenAccountAddress));
    Assert.assertEquals(crossContract.getOwnerChainId(),chainId);
    Assert.assertEquals(crossContract.getToChainId(),crossChainId);
    Assert.assertEquals(crossContract.getType(), CrossDataType.TOKEN);

    Long afterFromTokenBalance = PublicMethed.getAssetBalanceByAssetId(assetAccountId1,
        trc10TokenAccountKey,blockingStubFull);
    Long afterToTokenBalance = PublicMethed.getAssetBalanceByAssetId(assetAccountId2,
        trc10TokenAccountKey,crossBlockingStubFull);
    Long afterFromBalance = PublicMethed.queryAccount(trc10TokenAccountAddress,blockingStubFull)
        .getBalance();
    Long afterToBalance = PublicMethed.queryAccount(trc10TokenAccountAddress,crossBlockingStubFull)
        .getBalance();
    Optional<TransactionInfo> info = PublicMethed.getTransactionInfoById(txid, blockingStubFull);

    Assert.assertEquals(beforeToBalance,afterToBalance);
    Assert.assertEquals((Long)(beforeFromTokenBalance - afterFromTokenBalance),sendAmount);
    Assert.assertEquals((Long)(afterToTokenBalance - beforeToTokenBalance),sendAmount);
    Assert.assertEquals((Long)(beforeFromBalance - afterFromBalance),(Long)info.get().getFee());


    final Long afterBlockNum = blockingStubFull.getNowBlock(EmptyMessage.newBuilder()
        .build()).getBlockHeader().getRawData().getNumber();


    List<CrossMessage> crossMessageList =  getCrossMessageListFromTargetRange(beforeBlockNum,
        afterBlockNum,blockingStubFull);
    Assert.assertEquals(crossMessageList.size(),2);

    CrossMessage firstCrossMessage = crossMessageList.get(0);
    CrossMessage secondCrossMessage = crossMessageList.get(1);

    Assert.assertEquals(firstCrossMessage.getTransaction().getRawData().getContract(0),
        secondCrossMessage.getTransaction().getRawData().getContract(0));
    Assert.assertEquals(secondCrossMessage.getTransaction().getRawData().getSourceTxId(),
        ByteString.copyFrom(ByteArray.fromHexString(txid)));
    Assert.assertEquals(secondCrossMessage.getType(), Type.ACK);
  }


  @Test(enabled = true,description = "Transfer trc10 in cross chain with mutisign")
  public void test02CreateCrossTokenWithMutisign() throws InvalidProtocolBufferException {
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    final Long beforeFromTokenBalance = PublicMethed
        .getAssetBalanceByAssetId(mutisignAssetAccountId1, mutisignTestKey,blockingStubFull);
    final Long beforeToTokenBalance = PublicMethed
        .getAssetBalanceByAssetId(mutisignAssetAccountId2, mutisignTestKey,crossBlockingStubFull);
    final Long beforeFromBalance = PublicMethed
        .queryAccount(mutisignTestAddress,blockingStubFull).getBalance();
    final Long beforeToBalance = PublicMethed
        .queryAccount(mutisignTestAddress,crossBlockingStubFull).getBalance();

    final Long beforeBlockNum = blockingStubFull.getNowBlock(EmptyMessage.newBuilder()
        .build()).getBlockHeader().getRawData().getNumber();

    String txid = createCrossTrc10Transfer(mutisignTestAddress,
        mutisignTestAddress,mutisignAssetAccountId1,6,sendAmount * 2,name1,chainId,crossChainId,
        mutisignTestKey,2,permissionKeyString,blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(crossBlockingStubFull);


    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    //Query first chain
    Optional<Transaction> byId = PublicMethed.getTransactionById(txid, blockingStubFull);
    Any any = byId.get().getRawData().getContract(0).getParameter();
    BalanceContract.CrossContract crossContract = any.unpack(BalanceContract.CrossContract.class);
    Assert.assertEquals(crossContract.getOwnerAddress(),
        ByteString.copyFrom(mutisignTestAddress));
    Assert.assertEquals(crossContract.getToAddress(),
        ByteString.copyFrom(mutisignTestAddress));
    Assert.assertEquals(crossContract.getOwnerChainId(),chainId);
    Assert.assertEquals(crossContract.getToChainId(),crossChainId);
    Assert.assertEquals(crossContract.getType(), CrossDataType.TOKEN);

    //Query second chain
    byId = PublicMethed.getTransactionById(txid, crossBlockingStubFull);
    any = byId.get().getRawData().getContract(0).getParameter();
    crossContract = any.unpack(BalanceContract.CrossContract.class);
    Assert.assertEquals(crossContract.getOwnerAddress(),
        ByteString.copyFrom(mutisignTestAddress));
    Assert.assertEquals(crossContract.getToAddress(),
        ByteString.copyFrom(mutisignTestAddress));
    Assert.assertEquals(crossContract.getOwnerChainId(),chainId);
    Assert.assertEquals(crossContract.getToChainId(),crossChainId);
    Assert.assertEquals(crossContract.getType(), CrossDataType.TOKEN);

    Long afterFromTokenBalance = PublicMethed.getAssetBalanceByAssetId(mutisignAssetAccountId1,
        mutisignTestKey,blockingStubFull);
    Long afterToTokenBalance = PublicMethed.getAssetBalanceByAssetId(mutisignAssetAccountId2,
        mutisignTestKey,crossBlockingStubFull);
    Long afterFromBalance = PublicMethed.queryAccount(mutisignTestAddress,blockingStubFull)
        .getBalance();
    Long afterToBalance = PublicMethed.queryAccount(mutisignTestAddress,crossBlockingStubFull)
        .getBalance();
    Optional<TransactionInfo> info = PublicMethed.getTransactionInfoById(txid, blockingStubFull);

    Assert.assertEquals(beforeToBalance,afterToBalance);
    Assert.assertEquals((Long)(beforeFromTokenBalance
        - afterFromTokenBalance),(Long)(2 * sendAmount));
    Assert.assertEquals((Long)(afterToTokenBalance - beforeToTokenBalance),(Long)(2 * sendAmount));
    Assert.assertEquals((Long)(beforeFromBalance - afterFromBalance),(Long)info.get().getFee());
    Assert.assertEquals(info.get().getFee(),info.get().getReceipt().getNetFee() + 1000000L);


    final Long afterBlockNum = blockingStubFull.getNowBlock(EmptyMessage
        .newBuilder().build()).getBlockHeader().getRawData().getNumber();


    List<CrossMessage> crossMessageList =  getCrossMessageListFromTargetRange(beforeBlockNum,
        afterBlockNum,blockingStubFull);
    Assert.assertEquals(crossMessageList.size(),2);

    CrossMessage firstCrossMessage = crossMessageList.get(0);
    CrossMessage secondCrossMessage = crossMessageList.get(1);

    Assert.assertEquals(firstCrossMessage.getTransaction().getRawData().getContract(0),
        secondCrossMessage.getTransaction().getRawData().getContract(0));
    Assert.assertEquals(secondCrossMessage.getTransaction().getRawData().getSourceTxId(),
        ByteString.copyFrom(
        ByteArray.fromHexString(txid)));
    Assert.assertEquals(secondCrossMessage.getType(), Type.ACK);



  }

}

