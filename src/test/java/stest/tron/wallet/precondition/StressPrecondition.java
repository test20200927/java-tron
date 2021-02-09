package stest.tron.wallet.precondition;

import com.google.protobuf.ByteString;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.ExchangeList;
import org.tron.api.GrpcAPI.ProposalList;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletGrpc.WalletBlockingStub;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Configuration;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.ChainParameters;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;
import stest.tron.wallet.common.client.utils.PublicMethedForMutiSign;

@Slf4j
public class StressPrecondition {

  protected String commonOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.commonOwnerAddress");
  protected String triggerOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.triggerOwnerAddress");
  protected String triggerOwnerKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.triggerOwnerKey");
  protected String triggerRandomTrc20Address = Configuration.getByPath("stress.conf")
      .getString("address.triggerRandomTrc20Address");
  protected String triggerRandomTrc20Key = Configuration.getByPath("stress.conf")
      .getString("privateKey.triggerRandomTrc20Key");
  protected String commonOwnerPrivateKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.commonOwnerPrivateKey");
  protected String commonToAddress = Configuration.getByPath("stress.conf")
      .getString("address.commonToAddress");
  protected String commonToPrivateKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.commonToPrivateKey");
  protected String commonWitnessAddress = Configuration.getByPath("stress.conf")
      .getString("address.commonWitnessAddress");
  protected String commonWitnessPrivateKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.commonWitnessPrivateKey");

/*  protected String commonContractAddress1 = Configuration.getByPath("stress.conf")
      .getString("address.commonContractAddress1");
  protected String commonContractAddress2 = Configuration.getByPath("stress.conf")
      .getString("address.commonContractAddress2");
  protected String commonContractAddress3 = Configuration.getByPath("stress.conf")
      .getString("address.commonContractAddress3");
  protected String commontokenid = Configuration.getByPath("stress.conf")
      .getString("param.commontokenid");
  protected long commonexchangeid = Configuration.getByPath("stress.conf")
      .getLong("param.commonexchangeid");*/

  protected String delegateResourceAddress = Configuration.getByPath("stress.conf")
      .getString("address.delegateResourceAddress");
  protected String delegateResourceKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.delegateResourceKey");

  protected String assetIssueOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.assetIssueOwnerAddress");
  protected String assetIssueOwnerKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.assetIssueOwnerKey");
  protected String dexAssetIssueOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("dexAccount.dexAssetIssueOwnerAddress");
  protected String dexAssetIssueOwnerKey = Configuration.getByPath("stress.conf")
      .getString("dexAccount.dexAssetIssueOwnerKey");
  protected String participateOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.participateOwnerAddress");
  protected String participateOwnerPrivateKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.participateOwnerPrivateKey");
  protected String exchangeOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.exchangeOwnerAddress");
  protected String exchangeOwnerKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.exchangeOwnerKey");
  private String mutiSignOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.mutiSignOwnerAddress");
  private String mutiSignOwnerKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.mutiSignOwnerKey");
  private String voteOwnerKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.voteOwnerKey");
  private String voteOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.voteOwnerAddress");

  Long firstTokenInitialBalance = 500000000L;
  Long secondTokenInitialBalance = 500000000L;

  //TXtrbmfwZ2LxtoCveEhZT86fTss1w8rwJE
  String witnessKey001 = Configuration.getByPath("stress.conf").getString("permissioner.key1");
  //TWKKwLswTTcK5cp31F2bAteQrzU8cYhtU5
  String witnessKey002 = Configuration.getByPath("stress.conf").getString("permissioner.key2");
  //TT4MHXVApKfbcq7cDLKnes9h9wLSD4eMJi
  String witnessKey003 = Configuration.getByPath("stress.conf").getString("permissioner.key3");
  //TCw4yb4hS923FisfMsxAzQ85srXkK6RWGk
  String witnessKey004 = Configuration.getByPath("stress.conf").getString("permissioner.key4");
  //TLYUrci5Qw5fUPho2GvFv38kAK4QSmdhhN
  String witnessKey005 = Configuration.getByPath("stress.conf").getString("permissioner.key5");

  private final byte[] witness001Address = PublicMethed.getFinalAddress(witnessKey001);
  private final byte[] witness002Address = PublicMethed.getFinalAddress(witnessKey002);
  private final byte[] witness003Address = PublicMethed.getFinalAddress(witnessKey003);
  private final byte[] witness004Address = PublicMethed.getFinalAddress(witnessKey004);
  private final byte[] witness005Address = PublicMethed.getFinalAddress(witnessKey005);

  private ManagedChannel channelFull = null;

  private WalletGrpc.WalletBlockingStub blockingStubFull = null;


  private String oldAddress;
  private String newAddress;
  private String newContractAddress;
  private String fullnode = stest.tron.wallet.common.client.Configuration.getByPath("stress.conf")
    .getStringList("fullnode.ip.list").get(0);
  ByteString assetIssueId;
  Integer approveProposalIndex = 24;
  Optional<ExchangeList> listExchange;
  byte[] commonContractAddress1;
  byte[] commonContractAddress2;
  byte[] commonContractAddress3;
  byte[] commonContractAddress4;
  byte[] commonContractAddress5;
  byte[] commonContractAddress6;
  byte[] commonContractAddress7;
  byte[] commonContractAddress8;
  byte[] aggContractAddress;
  byte[] jstAddress;
  byte[] tstExchangeAddress;

  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  /**
   * constructor.
   */

  @BeforeClass
  public void beforeClass() {
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
  }

  @Test(enabled = true)
  public void test001CreateProposal() {
    ChainParameters chainParameters = blockingStubFull
        .getChainParameters(EmptyMessage.newBuilder().build());
    Optional<ChainParameters> getChainParameters = Optional.ofNullable(chainParameters);
    logger.info(Long.toString(getChainParameters.get().getChainParameterCount()));
    for (Integer i = 0; i < getChainParameters.get().getChainParameterCount(); i++) {
      logger.info("index:" + i);
      logger.info(getChainParameters.get().getChainParameter(i).getKey());
      logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
    }

    HashMap<Long, Long> proposalMap = new HashMap<Long, Long>();
    logger.info("--------------------------------------------");
    for (Integer i = 0; i < getChainParameters.get().getChainParameterCount(); i++) {
      if(getChainParameters.get().getChainParameter(i).getKey().equals("getAllowAdaptiveEnergy") && getChainParameters.get().getChainParameter(i).getValue() == 0) {
        logger.info(getChainParameters.get().getChainParameter(i).getKey());
        logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
        proposalMap.put(21L, 1L);
      }
      if(getChainParameters.get().getChainParameter(i).getKey().equals("getAllowShieldedTRC20Transaction") && getChainParameters.get().getChainParameter(i).getValue() == 0) {
        logger.info(getChainParameters.get().getChainParameter(i).getKey());
        logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
        proposalMap.put(39L, 1L);
      }
      if(getChainParameters.get().getChainParameter(i).getKey().equals("getAdaptiveResourceLimitTargetRatio") && getChainParameters.get().getChainParameter(i).getValue() == 10) {
        logger.info(getChainParameters.get().getChainParameter(i).getKey());
        logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
        proposalMap.put(33L, 6L);
      }
      if(getChainParameters.get().getChainParameter(i).getKey().equals("getAdaptiveResourceLimitMultiplier") && getChainParameters.get().getChainParameter(i).getValue() == 1000) {
        logger.info(getChainParameters.get().getChainParameter(i).getKey());
        logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
        proposalMap.put(29L, 20L);
      }
      if(getChainParameters.get().getChainParameter(i).getKey().equals("getAllowPBFT") && getChainParameters.get().getChainParameter(i).getValue() == 0) {
        logger.info(getChainParameters.get().getChainParameter(i).getKey());
        logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
        proposalMap.put(40L, 1L);
      }
      if(getChainParameters.get().getChainParameter(i).getKey().equals("getAllowMarketTransaction") && getChainParameters.get().getChainParameter(i).getValue() == 0) {
        logger.info(getChainParameters.get().getChainParameter(i).getKey());
        logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
        proposalMap.put(44L, 1L);
      }
      if(getChainParameters.get().getChainParameter(i).getKey().equals("getAllowTvmIstanbul") && getChainParameters.get().getChainParameter(i).getValue() == 0) {
        logger.info(getChainParameters.get().getChainParameter(i).getKey());
        logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
        proposalMap.put(41L, 1L);
      }
      if(getChainParameters.get().getChainParameter(i).getKey().equals("getMaxCpuTimeOfOneTx") && getChainParameters.get().getChainParameter(i).getValue() == 50) {
        logger.info(getChainParameters.get().getChainParameter(i).getKey());
        logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
        proposalMap.put(13L, 80L);
      }
      if(getChainParameters.get().getChainParameter(i).getKey().equals("getAllowTransactionFeePool") && getChainParameters.get().getChainParameter(i).getValue() == 0) {
        logger.info(getChainParameters.get().getChainParameter(i).getKey());
        logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
        proposalMap.put(48L, 1L);
        approveProposalIndex = i;

      }
      if(getChainParameters.get().getChainParameter(i).getKey().equals("getAllowOptimizeBlackHole") && getChainParameters.get().getChainParameter(i).getValue() == 0) {
        logger.info(getChainParameters.get().getChainParameter(i).getKey());
        logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
        proposalMap.put(49L, 1L);
        //approveProposalIndex = i;

      }
    }

    proposalMap.put(47L, 5000000000L);
    proposalMap.put(11L, 140L);
    proposalMap.put(3L, 140L);
    if (proposalMap.size() >= 1) {

      PublicMethed.createProposal(witness001Address, witnessKey001,
          proposalMap, blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      ProposalList proposalList = blockingStubFull.listProposals(EmptyMessage.newBuilder().build());
      Optional<ProposalList> listProposals = Optional.ofNullable(proposalList);
      final Integer proposalId = listProposals.get().getProposalsCount();

      String[] witnessList = {
          "541a2d585fcea7e9b1803df4eb49af0eb09f1fa2ce06aa5b8ed60ac95655d66d",
          "7d5a7396d6430edb7f66aa5736ef388f2bea862c9259de8ad8c2cfe080f6f5a0",
          "7c4977817417495f4ca0c35ab3d5a25e247355d68f89f593f3fea2ab62c8644f",
          "4521c13f65cc9f5c1daa56923b8598d4015801ad28379675c64106f5f6afec30",
          "f33101ea976d90491dcb9669be568db8bbc1ad23d90be4dede094976b67d550e",
          "1bb32958909299db452d3c9bbfd15fd745160d63e4985357874ee57708435a00",
          "29c91bd8b27c807d8dc2d2991aa0fbeafe7f54f4de9fac1e1684aa57242e3922",
          "97317d4d68a0c5ce14e74ad04dfc7521f142f5c0f247b632c8f94c755bdbe669",
          "1fe1d91bbe3ac4ac5dc9866c157ef7615ec248e3fd4f7d2b49b0428da5e046b2",
          "7c37ef485e186e07952bcc8e30cd911a6cd9f2a847736c89132762fb67a42329",
          "bcc142d57d872cd2cc1235bca454f2efd5a87f612856c979cc5b45a7399272a8",
          "6054824dc03546f903a06da1f405e72409379b83395d0bbb3d4563f56e828d52",
          "87cc8832b1b4860c3c69994bbfcdae9b520e6ce40cbe2a90566e707a7e04fc70",
          "c96c92c8a5f68ffba2ced3f7cd4baa6b784838a366f62914efdc79c6c18cd7d0",
          "d29e34899a21dc801c2be88184bed29a66246b5d85f26e8c77922ee2403a1934",
          "dc51f31e4de187c1c2530d65fb8f2958dff4c37f8cea430ce98d254baae37564",
          "ff43b371d67439bb8b6fa6c4ff615c954682008343d4cb2583b19f50adbac90f",
          "dbc78781ad27f3751358333412d5edc85b13e5eee129a1a77f7232baadafae0e",
          "a79a37a3d868e66456d76b233cb894d664b75fd91861340f3843db05ab3a8c66",
          "a8107ea1c97c90cd4d84e79cd79d327def6362cc6fd498fc3d3766a6a71924f6",
          "b5076206430b2ca069ae2f4dc6f20dd0d74551559878990d1df12a723c228039",
          "442513e2e801bc42d14d33b8148851dae756d08eeb48881a44e1b2002b3fb700"
      };


      for(String witnessKey : witnessList) {
        byte[] witnessAddress = PublicMethed.getFinalAddress(witnessKey);
        PublicMethed.approveProposal(witnessAddress, witnessKey, proposalId,
            true, blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);
      }

      waitProposalApprove(approveProposalIndex, blockingStubFull);
    }
  }

  @Test(enabled = true)
  public void test002SendCoinToStressAccount() {
    sendCoinToStressAccount(commonOwnerPrivateKey);
    sendCoinToStressAccount(triggerOwnerKey);
    sendCoinToStressAccount(commonToPrivateKey);
    sendCoinToStressAccount(assetIssueOwnerKey);
    sendCoinToStressAccount(participateOwnerPrivateKey);
    sendCoinToStressAccount(exchangeOwnerKey);
    sendCoinToStressAccount(mutiSignOwnerKey);
    sendCoinToStressAccount(dexAssetIssueOwnerKey);
    sendCoinToStressAccount(triggerRandomTrc20Key);
    logger.info(
        "commonOwnerAddress " + PublicMethed.queryAccount(commonOwnerPrivateKey, blockingStubFull)
            .getBalance());
    logger.info(
        "triggerOwnerAddress " + PublicMethed.queryAccount(triggerOwnerKey, blockingStubFull)
            .getBalance());
    logger.info("commonToAddress " + PublicMethed.queryAccount(commonToPrivateKey, blockingStubFull)
        .getBalance());
    logger.info(
        "assetIssueOwnerAddress " + PublicMethed.queryAccount(assetIssueOwnerKey, blockingStubFull)
            .getBalance());
    logger.info("participateOwnerAddress " + PublicMethed
        .queryAccount(participateOwnerPrivateKey, blockingStubFull).getBalance());
    logger.info("exchangeOwnerKey " + PublicMethed.queryAccount(exchangeOwnerKey, blockingStubFull)
        .getBalance());
    logger.info("mutiSignOwnerKey " + PublicMethed.queryAccount(mutiSignOwnerKey, blockingStubFull)
        .getBalance());
    logger.info("triggerRandomTrc20Key " + PublicMethed.queryAccount(triggerRandomTrc20Key, blockingStubFull)
        .getBalance());
    PublicMethed
        .freezeBalanceGetEnergy(PublicMethed.getFinalAddress(triggerOwnerKey), 50000000000000L, 3,
            1, triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed
        .freezeBalanceGetEnergy(PublicMethed.getFinalAddress(triggerOwnerKey), 50000000000000L, 3,
            0, triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed
        .freezeBalanceGetEnergy(PublicMethed.getFinalAddress(voteOwnerKey), 10000000000000L, 3,
            0, voteOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test003DeploySmartContract1() {
    String contractName = "tokenTest";
    String code = "608060405260e2806100126000396000f300608060405260043610603e5763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416633be9ece781146043575b600080fd5b606873ffffffffffffffffffffffffffffffffffffffff60043516602435604435606a565b005b60405173ffffffffffffffffffffffffffffffffffffffff84169082156108fc029083908590600081818185878a8ad094505050505015801560b0573d6000803e3d6000fd5b505050505600a165627a7a72305820d7ac1a3b49eeff286b7f2402b93047e60deb6dba47f4f889d921dbcb3bb81f8a0029";
    String abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"toAddress\",\"type\":\"address\"},{\"name\":\"id\",\"type\":\"trcToken\"},{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"TransferTokenTo\",\"outputs\":[],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"inputs\":[],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"constructor\"}]";
    commonContractAddress1 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L,
            0L, 100, 10000, "0",
            0, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey),
            blockingStubFull);

    newContractAddress = WalletClient.encode58Check(commonContractAddress1);

    oldAddress = readWantedText("stress.conf", "commonContractAddress1");
    newAddress = "  commonContractAddress1 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

  }

  @Test(enabled = true)
  public void test004DeploySmartContract2() {
    String contractName = "BTest";
    String code = "60806040526000805560c5806100166000396000f30060806040526004361060485763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166362548c7b8114604a578063890eba68146050575b005b6048608c565b348015605b57600080fd5b50d38015606757600080fd5b50d28015607357600080fd5b50607a6093565b60408051918252519081900360200190f35b6001600055565b600054815600a165627a7a723058204c4f1bb8eca0c4f1678cc7cc1179e03d99da2a980e6792feebe4d55c89c022830029";
    String abi = "[{\"constant\":false,\"inputs\":[],\"name\":\"setFlag\",\"outputs\":[],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"flag\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"constructor\"},{\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"fallback\"}]";
    commonContractAddress2 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L,
            0L, 100, 10000, "0",
            0, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey),
            blockingStubFull);

    newContractAddress = WalletClient.encode58Check(commonContractAddress2);

    oldAddress = readWantedText("stress.conf", "commonContractAddress2");
    newAddress = "  commonContractAddress2 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test005DeploySmartContract3() {
    String contractName = "TestSStore";
    String code = "608060405234801561001057600080fd5b5061045c806100206000396000f30060806040526004361061006d576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806304c58438146100725780634f2be91f1461009f578063812db772146100b657806393cd5755146100e3578063d1cd64e914610189575b600080fd5b34801561007e57600080fd5b5061009d600480360381019080803590602001909291905050506101a0565b005b3480156100ab57600080fd5b506100b4610230565b005b3480156100c257600080fd5b506100e1600480360381019080803590602001909291905050506102a2565b005b3480156100ef57600080fd5b5061010e600480360381019080803590602001909291905050506102c3565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561014e578082015181840152602081019050610133565b50505050905090810190601f16801561017b5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561019557600080fd5b5061019e61037e565b005b6000600190505b8181101561022c5760008060018154018082558091505090600182039060005260206000200160006040805190810160405280600881526020017f31323334353637380000000000000000000000000000000000000000000000008152509091909150908051906020019061021d92919061038b565b505080806001019150506101a7565b5050565b60008060018154018082558091505090600182039060005260206000200160006040805190810160405280600881526020017f61626364656667680000000000000000000000000000000000000000000000008152509091909150908051906020019061029e92919061038b565b5050565b6000600190505b81811115156102bf5780806001019150506102a9565b5050565b6000818154811015156102d257fe5b906000526020600020016000915090508054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156103765780601f1061034b57610100808354040283529160200191610376565b820191906000526020600020905b81548152906001019060200180831161035957829003601f168201915b505050505081565b6000808060010191505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106103cc57805160ff19168380011785556103fa565b828001600101855582156103fa579182015b828111156103f95782518255916020019190600101906103de565b5b509050610407919061040b565b5090565b61042d91905b80821115610429576000816000905550600101610411565b5090565b905600a165627a7a7230582087d9880a135295a17100f63b8941457f4369204d3ccc9ce4a1abf99820eb68480029";
    String abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"index\",\"type\":\"uint256\"}],\"name\":\"add2\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"add\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"index\",\"type\":\"uint256\"}],\"name\":\"fori2\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"args\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"fori\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";
    commonContractAddress3 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L,
            0L, 100, 10000, "0",
            0, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey),
            blockingStubFull);

    newContractAddress = WalletClient.encode58Check(commonContractAddress3);

    oldAddress = readWantedText("stress.conf", "commonContractAddress3");
    newAddress = "  commonContractAddress3 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test006CreateTokenAndDispatchToken() {
    if (PublicMethed.queryAccount(assetIssueOwnerKey, blockingStubFull).getAssetIssuedID()
        .isEmpty()) {
      Long start = System.currentTimeMillis() + 20000;
      Long end = System.currentTimeMillis() + 1000000000;
      PublicMethed.createAssetIssue(PublicMethed.getFinalAddress(assetIssueOwnerKey), "xxd",
          50000000000000000L,
          1, 1, start, end, 1, "wwwwww", "wwwwwwww", 100000L,
          100000L, 1L, 1L, assetIssueOwnerKey, blockingStubFull);
      logger.info("createAssetIssue");
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
    }

    assetIssueId = PublicMethed.queryAccount(assetIssueOwnerKey, blockingStubFull)
        .getAssetIssuedID();
    logger.info("AssetIssueId is " + ByteArray.toStr(assetIssueId.toByteArray()));


    if (PublicMethed.queryAccount(dexAssetIssueOwnerKey, blockingStubFull).getAssetIssuedID()
        .isEmpty()) {
      Long start = System.currentTimeMillis() + 20000;
      Long end = System.currentTimeMillis() + 1000000000;
      PublicMethed.createAssetIssue(PublicMethed.getFinalAddress(dexAssetIssueOwnerKey), "xxxd",
          50000000000000000L,
          1, 1, start, end, 1, "wwwwww", "wwwwwwww", 100000L,
          100000L, 1L, 1L, dexAssetIssueOwnerKey, blockingStubFull);
      logger.info("createAssetIssue dex");
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
    }

    ByteString dexAssetIssueId = PublicMethed
        .queryAccount(dexAssetIssueOwnerKey, blockingStubFull)
        .getAssetIssuedID();
    logger.info("dex AssetIssueId is " + ByteArray.toStr(dexAssetIssueId.toByteArray()));

    logger.info("commonContractAddress1 is " + Wallet.encode58Check(commonContractAddress1));
    PublicMethed.transferAsset(commonContractAddress1, assetIssueId.toByteArray(), 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress1, assetIssueId.toByteArray(), 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress1, assetIssueId.toByteArray(), 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress1, assetIssueId.toByteArray(), 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress2, assetIssueId.toByteArray(), 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress2, assetIssueId.toByteArray(), 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress2, assetIssueId.toByteArray(), 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress2, assetIssueId.toByteArray(), 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);

    for(int i  = 0; i <= 9;i++) {
      String dexKey = Configuration.getByPath("stress.conf")
          .getString("dexAccount.dexAccount" + i + "Key");
      PublicMethed.transferAsset(PublicMethed.getFinalAddress(dexKey), assetIssueId.toByteArray(), 30000000000L,
          PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
      PublicMethed.transferAsset(PublicMethed.getFinalAddress(dexKey),
          dexAssetIssueId.toByteArray(), 30000000000L,
          PublicMethed.getFinalAddress(dexAssetIssueOwnerKey), dexAssetIssueOwnerKey,
          blockingStubFull);
      sendCoinToStressAccount(dexKey);
    }



    String newTokenId = ByteArray.toStr(assetIssueId.toByteArray());
    String oldTokenIdString = readWantedText("stress.conf", "commontokenid");
    logger.info("oldTokenIdString " + oldTokenIdString);
    String newTokenIdInConfig = "commontokenid = " + newTokenId;
    logger.info("newTokenIdInConfig " + newTokenIdInConfig);
    replacAddressInConfig("stress.conf", oldTokenIdString, newTokenIdInConfig);

    newTokenId = ByteArray.toStr(dexAssetIssueId.toByteArray());
    oldTokenIdString = readWantedText("stress.conf", "dextokenid");
    logger.info("oldTokenIdString " + oldTokenIdString);
    newTokenIdInConfig = "dextokenid = " + newTokenId;
    logger.info("newTokenIdInConfig " + newTokenIdInConfig);
    replacAddressInConfig("stress.conf", oldTokenIdString, newTokenIdInConfig);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test007CreateExchange() {
    listExchange = PublicMethed.getExchangeList(blockingStubFull);
    Long exchangeId = 0L;
    assetIssueId = PublicMethed.queryAccount(exchangeOwnerKey, blockingStubFull).getAssetIssuedID();

    for (Integer i = 0; i < listExchange.get().getExchangesCount(); i++) {
      if (ByteArray.toHexString(listExchange.get().getExchanges(i)
          .getCreatorAddress().toByteArray()).equalsIgnoreCase(
          ByteArray.toHexString(PublicMethed.getFinalAddress(exchangeOwnerKey)))) {
        logger.info("id is " + listExchange.get().getExchanges(i).getExchangeId());
        exchangeId = listExchange.get().getExchanges(i).getExchangeId();
        break;
      }
    }

    if (exchangeId == 0L) {
      String trx = "_";
      byte[] b = trx.getBytes();
      PublicMethed.exchangeCreate(assetIssueId.toByteArray(), firstTokenInitialBalance,
          b, secondTokenInitialBalance, PublicMethed.getFinalAddress(exchangeOwnerKey),
          exchangeOwnerKey, blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      listExchange = PublicMethed.getExchangeList(blockingStubFull);
      for (Integer i = 0; i < listExchange.get().getExchangesCount(); i++) {
        if (ByteArray.toHexString(listExchange.get().getExchanges(i)
            .getCreatorAddress().toByteArray()).equalsIgnoreCase(
            ByteArray.toHexString(PublicMethed.getFinalAddress(exchangeOwnerKey)))) {
          logger.info("id is " + listExchange.get().getExchanges(i).getExchangeId());
          exchangeId = listExchange.get().getExchanges(i).getExchangeId();
          break;
        }
      }
    }

    String newExchangeId = "" + exchangeId;
    String oldExchangeIdString = readWantedText("stress.conf", "commonexchangeid");
    logger.info("oldExchangeIdString " + oldExchangeIdString);
    String newTokenIdInConfig = "commonexchangeid = " + newExchangeId;
    logger.info("newTokenIdInConfig " + newTokenIdInConfig);
    replacAddressInConfig("stress.conf", oldExchangeIdString, newTokenIdInConfig);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }


  @Test(enabled = true)
  public void test008MutiSignUpdate() {
    String[] permissionKeyString = new String[5];
    String[] ownerKeyString = new String[1];
    permissionKeyString[0] = witnessKey001;
    permissionKeyString[1] = witnessKey002;
    permissionKeyString[2] = witnessKey003;
    permissionKeyString[3] = witnessKey004;
    permissionKeyString[4] = witnessKey005;

    ownerKeyString[0] = mutiSignOwnerKey;

    String accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":5,\"keys\":["
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey001) + "\",\"weight\":1},"
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey002) + "\",\"weight\":1}"
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey003) + "\",\"weight\":1}"
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey004) + "\",\"weight\":1}"
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey005)
            + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":2,"
            + "\"operations\":\"7fff1fc0033e0000000000000000000000000000000000000000000000000000\","
            + "\"keys\":["
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey001) + "\",\"weight\":1},"
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey002) + "\",\"weight\":1}"
            + "]}]}";

    logger.info(accountPermissionJson);
    PublicMethedForMutiSign.accountPermissionUpdate(accountPermissionJson,
        PublicMethed.getFinalAddress(mutiSignOwnerKey), mutiSignOwnerKey,
        blockingStubFull, ownerKeyString);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

  }

  @Test(enabled = true)
  public void test009DeploySmartContract4() {
    String contractName = "TRC20_TRON";
    String abi = "[{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":false,\"inputs\":[],\"name\":\"stop\",\"outputs\":[],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":false,\"inputs\":[{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"burn\",\"outputs\":[],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":true,\"inputs\":[],\"name\":\"stopped\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":false,\"inputs\":[],\"name\":\"start\",\"outputs\":[],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":false,\"inputs\":[{\"name\":\"_name\",\"type\":\"string\"}],\"name\":\"setName\",\"outputs\":[],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"inputs\":[],\"payable\":false,\"type\":\"constructor\",\"stateMutability\":\"nonpayable\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"}]";
    String code = "6060604052604060405190810160405280600681526020017f54726f6e697800000000000000000000000000000000000000000000000000008152506000908051906020019062000052929190620001b6565b50604060405190810160405280600381526020017f545258000000000000000000000000000000000000000000000000000000000081525060019080519060200190620000a1929190620001b6565b50600660025560006005556000600660006101000a81548160ff0219169083151502179055506000600660016101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555034156200011257fe5b5b33600660016101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555067016345785d8a000060058190555067016345785d8a0000600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055505b62000265565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10620001f957805160ff19168380011785556200022a565b828001600101855582156200022a579182015b82811115620002295782518255916020019190600101906200020c565b5b5090506200023991906200023d565b5090565b6200026291905b808211156200025e57600081600090555060010162000244565b5090565b90565b61111480620002756000396000f300606060405236156100ce576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806306fdde03146100d057806307da68f514610169578063095ea7b31461017b57806318160ddd146101d257806323b872dd146101f8578063313ce5671461026e57806342966c681461029457806370a08231146102b457806375f12b21146102fe57806395d89b4114610328578063a9059cbb146103c1578063be9a655514610418578063c47f00271461042a578063dd62ed3e14610484575bfe5b34156100d857fe5b6100e06104ed565b604051808060200182810382528381815181526020019150805190602001908083836000831461012f575b80518252602083111561012f5760208201915060208101905060208303925061010b565b505050905090810190601f16801561015b5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561017157fe5b61017961058b565b005b341561018357fe5b6101b8600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091908035906020019091905050610603565b604051808215151515815260200191505060405180910390f35b34156101da57fe5b6101e26107cb565b6040518082815260200191505060405180910390f35b341561020057fe5b610254600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803573ffffffffffffffffffffffffffffffffffffffff169060200190919080359060200190919050506107d1565b604051808215151515815260200191505060405180910390f35b341561027657fe5b61027e610b11565b6040518082815260200191505060405180910390f35b341561029c57fe5b6102b26004808035906020019091905050610b17565b005b34156102bc57fe5b6102e8600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610c3f565b6040518082815260200191505060405180910390f35b341561030657fe5b61030e610c57565b604051808215151515815260200191505060405180910390f35b341561033057fe5b610338610c6a565b6040518080602001828103825283818151815260200191508051906020019080838360008314610387575b80518252602083111561038757602082019150602081019050602083039250610363565b505050905090810190601f1680156103b35780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34156103c957fe5b6103fe600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091908035906020019091905050610d08565b604051808215151515815260200191505060405180910390f35b341561042057fe5b610428610f31565b005b341561043257fe5b610482600480803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091905050610fa9565b005b341561048c57fe5b6104d7600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803573ffffffffffffffffffffffffffffffffffffffff1690602001909190505061101e565b6040518082815260200191505060405180910390f35b60008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156105835780601f1061055857610100808354040283529160200191610583565b820191906000526020600020905b81548152906001019060200180831161056657829003601f168201915b505050505081565b3373ffffffffffffffffffffffffffffffffffffffff16600660019054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161415156105e457fe5b6001600660006101000a81548160ff0219169083151502179055505b5b565b6000600660009054906101000a900460ff1615151561061e57fe5b3373ffffffffffffffffffffffffffffffffffffffff1660001415151561064157fe5b60008214806106cc57506000600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054145b15156106d85760006000fd5b81600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925846040518082815260200191505060405180910390a3600190505b5b5b92915050565b60055481565b6000600660009054906101000a900460ff161515156107ec57fe5b3373ffffffffffffffffffffffffffffffffffffffff1660001415151561080f57fe5b81600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020541015151561085e5760006000fd5b600360008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205482600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205401101515156108ee5760006000fd5b81600460008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020541015151561097a5760006000fd5b81600360008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254019250508190555081600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254039250508190555081600460008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825403925050819055508273ffffffffffffffffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef846040518082815260200191505060405180910390a3600190505b5b5b9392505050565b60025481565b80600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205410151515610b665760006000fd5b80600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825403925050819055508060036000600073ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254019250508190555060003373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef836040518082815260200191505060405180910390a35b50565b60036020528060005260406000206000915090505481565b600660009054906101000a900460ff1681565b60018054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015610d005780601f10610cd557610100808354040283529160200191610d00565b820191906000526020600020905b815481529060010190602001808311610ce357829003601f168201915b505050505081565b6000600660009054906101000a900460ff16151515610d2357fe5b3373ffffffffffffffffffffffffffffffffffffffff16600014151515610d4657fe5b81600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205410151515610d955760006000fd5b600360008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205482600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020540110151515610e255760006000fd5b81600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254039250508190555081600360008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825401925050819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef846040518082815260200191505060405180910390a3600190505b5b5b92915050565b3373ffffffffffffffffffffffffffffffffffffffff16600660019054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16141515610f8a57fe5b6000600660006101000a81548160ff0219169083151502179055505b5b565b3373ffffffffffffffffffffffffffffffffffffffff16600660019054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614151561100257fe5b8060009080519060200190611018929190611043565b505b5b50565b6004602052816000526040600020602052806000526040600020600091509150505481565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061108457805160ff19168380011785556110b2565b828001600101855582156110b2579182015b828111156110b1578251825591602001919060010190611096565b5b5090506110bf91906110c3565b5090565b6110e591905b808211156110e15760008160009055506001016110c9565b5090565b905600a165627a7a723058204858328431ff0a4e0db74ff432e5805ce4bcf91a1c59650a93bd7c1aec5e0fe10029";
    commonContractAddress4 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L,
            0L, 100, 10000, "0",
            0, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey),
            blockingStubFull);

    newContractAddress = WalletClient.encode58Check(commonContractAddress4);

    oldAddress = readWantedText("stress.conf", "commonContractAddress4");
    newAddress = "  commonContractAddress4 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test010DeploySmartContract5() {
    String contractName = "Trigger";
    String code = stest.tron.wallet.common.client.Configuration.getByPath("stress.conf")
        .getString("code.code_veryLarge");
    String abi = stest.tron.wallet.common.client.Configuration.getByPath("stress.conf")
        .getString("abi.abi_veryLarge");

    commonContractAddress5 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L, 0L, 100, 10000, "0", 0, null,
            triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    newContractAddress = WalletClient.encode58Check(commonContractAddress5);

    oldAddress = readWantedText("stress.conf", "commonContractAddress5");
    newAddress = "  commonContractAddress5 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

  }

  @Test(enabled = true)
  public void test011DeploySmartContract6() {
    String contractName = "Trigger";
    String abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"test\",\"outputs\":[{\"name\":\"i\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"addrs\",\"type\":\"address[]\"}],\"name\":\"test\",\"outputs\":[{\"name\":\"i\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String code = "608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b506101df8061003a6000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b506004361061006c577c01000000000000000000000000000000000000000000000000000000006000350463bb29998e8114610071578063d57498ea146100b6575b600080fd5b6100a46004803603602081101561008757600080fd5b503573ffffffffffffffffffffffffffffffffffffffff16610159565b60408051918252519081900360200190f35b6100a4600480360360208110156100cc57600080fd5b8101906020810181356401000000008111156100e757600080fd5b8201836020820111156100f957600080fd5b8035906020019184602083028401116401000000008311171561011b57600080fd5b919080806020026020016040519081016040528093929190818152602001838360200280828437600092019190915250929550610178945050505050565b6000805b6103e85a11156101725750600101813f61015d565b50919050565b600080805b83518110156101ac576000848281518110151561019657fe5b602090810290910101513f92505060010161017d565b939250505056fea165627a7a7230582033651916fb1624df072a51c976207dd49ce0af4f3479f46a4f81f293afcc5f2b0029";
    commonContractAddress6 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L, 0L, 100, 10000, "0", 0, null,
            triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    newContractAddress = WalletClient.encode58Check(commonContractAddress6);

    oldAddress = readWantedText("stress.conf", "commonContractAddress6");
    newAddress = "  commonContractAddress6 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test012DeployDifferentSmartContract() {
    Integer contractCount = 200;
    while (contractCount > 0) {
      String contractName = "TRC20_TRON";
      String abi = "[{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":false,\"inputs\":[],\"name\":\"stop\",\"outputs\":[],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":false,\"inputs\":[{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"burn\",\"outputs\":[],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":true,\"inputs\":[],\"name\":\"stopped\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":false,\"inputs\":[],\"name\":\"start\",\"outputs\":[],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":false,\"inputs\":[{\"name\":\"_name\",\"type\":\"string\"}],\"name\":\"setName\",\"outputs\":[],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"inputs\":[],\"payable\":false,\"type\":\"constructor\",\"stateMutability\":\"nonpayable\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"}]";
      String code = "6060604052604060405190810160405280600681526020017f54726f6e697800000000000000000000000000000000000000000000000000008152506000908051906020019062000052929190620001b6565b50604060405190810160405280600381526020017f545258000000000000000000000000000000000000000000000000000000000081525060019080519060200190620000a1929190620001b6565b50600660025560006005556000600660006101000a81548160ff0219169083151502179055506000600660016101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555034156200011257fe5b5b33600660016101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555067016345785d8a000060058190555067016345785d8a0000600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055505b62000265565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10620001f957805160ff19168380011785556200022a565b828001600101855582156200022a579182015b82811115620002295782518255916020019190600101906200020c565b5b5090506200023991906200023d565b5090565b6200026291905b808211156200025e57600081600090555060010162000244565b5090565b90565b61111480620002756000396000f300606060405236156100ce576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806306fdde03146100d057806307da68f514610169578063095ea7b31461017b57806318160ddd146101d257806323b872dd146101f8578063313ce5671461026e57806342966c681461029457806370a08231146102b457806375f12b21146102fe57806395d89b4114610328578063a9059cbb146103c1578063be9a655514610418578063c47f00271461042a578063dd62ed3e14610484575bfe5b34156100d857fe5b6100e06104ed565b604051808060200182810382528381815181526020019150805190602001908083836000831461012f575b80518252602083111561012f5760208201915060208101905060208303925061010b565b505050905090810190601f16801561015b5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561017157fe5b61017961058b565b005b341561018357fe5b6101b8600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091908035906020019091905050610603565b604051808215151515815260200191505060405180910390f35b34156101da57fe5b6101e26107cb565b6040518082815260200191505060405180910390f35b341561020057fe5b610254600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803573ffffffffffffffffffffffffffffffffffffffff169060200190919080359060200190919050506107d1565b604051808215151515815260200191505060405180910390f35b341561027657fe5b61027e610b11565b6040518082815260200191505060405180910390f35b341561029c57fe5b6102b26004808035906020019091905050610b17565b005b34156102bc57fe5b6102e8600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610c3f565b6040518082815260200191505060405180910390f35b341561030657fe5b61030e610c57565b604051808215151515815260200191505060405180910390f35b341561033057fe5b610338610c6a565b6040518080602001828103825283818151815260200191508051906020019080838360008314610387575b80518252602083111561038757602082019150602081019050602083039250610363565b505050905090810190601f1680156103b35780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34156103c957fe5b6103fe600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091908035906020019091905050610d08565b604051808215151515815260200191505060405180910390f35b341561042057fe5b610428610f31565b005b341561043257fe5b610482600480803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091905050610fa9565b005b341561048c57fe5b6104d7600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803573ffffffffffffffffffffffffffffffffffffffff1690602001909190505061101e565b6040518082815260200191505060405180910390f35b60008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156105835780601f1061055857610100808354040283529160200191610583565b820191906000526020600020905b81548152906001019060200180831161056657829003601f168201915b505050505081565b3373ffffffffffffffffffffffffffffffffffffffff16600660019054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161415156105e457fe5b6001600660006101000a81548160ff0219169083151502179055505b5b565b6000600660009054906101000a900460ff1615151561061e57fe5b3373ffffffffffffffffffffffffffffffffffffffff1660001415151561064157fe5b60008214806106cc57506000600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054145b15156106d85760006000fd5b81600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925846040518082815260200191505060405180910390a3600190505b5b5b92915050565b60055481565b6000600660009054906101000a900460ff161515156107ec57fe5b3373ffffffffffffffffffffffffffffffffffffffff1660001415151561080f57fe5b81600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020541015151561085e5760006000fd5b600360008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205482600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205401101515156108ee5760006000fd5b81600460008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020541015151561097a5760006000fd5b81600360008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254019250508190555081600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254039250508190555081600460008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825403925050819055508273ffffffffffffffffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef846040518082815260200191505060405180910390a3600190505b5b5b9392505050565b60025481565b80600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205410151515610b665760006000fd5b80600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825403925050819055508060036000600073ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254019250508190555060003373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef836040518082815260200191505060405180910390a35b50565b60036020528060005260406000206000915090505481565b600660009054906101000a900460ff1681565b60018054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015610d005780601f10610cd557610100808354040283529160200191610d00565b820191906000526020600020905b815481529060010190602001808311610ce357829003601f168201915b505050505081565b6000600660009054906101000a900460ff16151515610d2357fe5b3373ffffffffffffffffffffffffffffffffffffffff16600014151515610d4657fe5b81600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205410151515610d955760006000fd5b600360008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205482600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020540110151515610e255760006000fd5b81600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254039250508190555081600360008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825401925050819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef846040518082815260200191505060405180910390a3600190505b5b5b92915050565b3373ffffffffffffffffffffffffffffffffffffffff16600660019054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16141515610f8a57fe5b6000600660006101000a81548160ff0219169083151502179055505b5b565b3373ffffffffffffffffffffffffffffffffffffffff16600660019054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614151561100257fe5b8060009080519060200190611018929190611043565b505b5b50565b6004602052816000526040600020602052806000526040600020600091509150505481565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061108457805160ff19168380011785556110b2565b828001600101855582156110b2579182015b828111156110b1578251825591602001919060010190611096565b5b5090506110bf91906110c3565b5090565b6110e591905b808211156110e15760008160009055506001016110c9565b5090565b905600a165627a7a723058204858328431ff0a4e0db74ff432e5805ce4bcf91a1c59650a93bd7c1aec5e0fe10029";
      commonContractAddress4 = PublicMethed
          .deployContract(contractName, abi, code, "", 1000000000L,
              0L, 100, 10000, "0",
              0, null, triggerRandomTrc20Key, PublicMethed.getFinalAddress(triggerRandomTrc20Key),
              blockingStubFull);
      try {
        newContractAddress = WalletClient.encode58Check(commonContractAddress4);
      } catch (Exception e) {
        continue;
      }
      contractCount--;
      writeTrc20ContractToFile(newContractAddress);
      logger.info("Trc 20 contract index :" + contractCount);
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }



  @Test(enabled = true)
  public void test013ChangeMaintainTime() {
    ChainParameters chainParameters = blockingStubFull
        .getChainParameters(EmptyMessage.newBuilder().build());
    Optional<ChainParameters> getChainParameters = Optional.ofNullable(chainParameters);
    logger.info(Long.toString(getChainParameters.get().getChainParameterCount()));
    for (Integer i = 0; i < getChainParameters.get().getChainParameterCount(); i++) {
      logger.info("index:" + i);
      logger.info(getChainParameters.get().getChainParameter(i).getKey());
      logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
    }

    HashMap<Long, Long> proposalMap = new HashMap<Long, Long>();
    logger.info("--------------------------------------------");
    for (Integer i = 0; i < getChainParameters.get().getChainParameterCount(); i++) {
      if(getChainParameters.get().getChainParameter(i).getKey().equals("getMaintenanceTimeInterval") && getChainParameters.get().getChainParameter(i).getValue() == 300000) {
        logger.info(getChainParameters.get().getChainParameter(i).getKey());
        logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
        proposalMap.put(0L, 300000 * 6 * 1L);
      }

    }

    if (proposalMap.size() >= 1) {

      PublicMethed.createProposal(witness001Address, witnessKey001,
          proposalMap, blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      ProposalList proposalList = blockingStubFull.listProposals(EmptyMessage.newBuilder().build());
      Optional<ProposalList> listProposals = Optional.ofNullable(proposalList);
      final Integer proposalId = listProposals.get().getProposalsCount();

      String[] witnessList = {
          "541a2d585fcea7e9b1803df4eb49af0eb09f1fa2ce06aa5b8ed60ac95655d66d",
          "7d5a7396d6430edb7f66aa5736ef388f2bea862c9259de8ad8c2cfe080f6f5a0",
          "7c4977817417495f4ca0c35ab3d5a25e247355d68f89f593f3fea2ab62c8644f",
          "4521c13f65cc9f5c1daa56923b8598d4015801ad28379675c64106f5f6afec30",
          "f33101ea976d90491dcb9669be568db8bbc1ad23d90be4dede094976b67d550e",
          "1bb32958909299db452d3c9bbfd15fd745160d63e4985357874ee57708435a00",
          "29c91bd8b27c807d8dc2d2991aa0fbeafe7f54f4de9fac1e1684aa57242e3922",
          "97317d4d68a0c5ce14e74ad04dfc7521f142f5c0f247b632c8f94c755bdbe669",
          "1fe1d91bbe3ac4ac5dc9866c157ef7615ec248e3fd4f7d2b49b0428da5e046b2",
          "7c37ef485e186e07952bcc8e30cd911a6cd9f2a847736c89132762fb67a42329",
          "bcc142d57d872cd2cc1235bca454f2efd5a87f612856c979cc5b45a7399272a8",
          "6054824dc03546f903a06da1f405e72409379b83395d0bbb3d4563f56e828d52",
          "87cc8832b1b4860c3c69994bbfcdae9b520e6ce40cbe2a90566e707a7e04fc70",
          "c96c92c8a5f68ffba2ced3f7cd4baa6b784838a366f62914efdc79c6c18cd7d0",
          "d29e34899a21dc801c2be88184bed29a66246b5d85f26e8c77922ee2403a1934",
          "dc51f31e4de187c1c2530d65fb8f2958dff4c37f8cea430ce98d254baae37564",
          "ff43b371d67439bb8b6fa6c4ff615c954682008343d4cb2583b19f50adbac90f",
          "dbc78781ad27f3751358333412d5edc85b13e5eee129a1a77f7232baadafae0e",
          "a79a37a3d868e66456d76b233cb894d664b75fd91861340f3843db05ab3a8c66",
          "a8107ea1c97c90cd4d84e79cd79d327def6362cc6fd498fc3d3766a6a71924f6",
          "b5076206430b2ca069ae2f4dc6f20dd0d74551559878990d1df12a723c228039",
          "442513e2e801bc42d14d33b8148851dae756d08eeb48881a44e1b2002b3fb700"
      };


      for(String witnessKey : witnessList) {
        byte[] witnessAddress = PublicMethed.getFinalAddress(witnessKey);
        PublicMethed.approveProposal(witnessAddress, witnessKey, proposalId,
            true, blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);
      }

    }
  }


  @Test(enabled = false)
  public void test014CreateSrUsedToken() {

    ManagedChannel channelFull = ManagedChannelBuilder.forTarget("47.95.206.44:50051")
        .usePlaintext(true)
        .build();

    WalletBlockingStub blockingStubFull = WalletGrpc.newBlockingStub(channelFull);

    GrpcAPI.WitnessList witnessList= blockingStubFull.listWitnesses(EmptyMessage.newBuilder().build());

    HashMap<ByteString,Long> map = new HashMap<>();

    for(int i = 0; i < witnessList.getWitnessesCount();i++) {
      logger.info("count:" + witnessList.getWitnesses(i).getVoteCount());
      map.put(witnessList.getWitnesses(i).getAddress(),witnessList.getWitnesses(i).getVoteCount());
    }
    List<Map.Entry<ByteString,Long>> list=new ArrayList<>();
    list.addAll(map.entrySet());
    ValueComparator vc=new ValueComparator();
    Collections.sort(list,vc);

    logger.info("-----------------------------------");
    int number = 0;
    for(Iterator<Entry<ByteString,Long>> it=list.iterator();it.hasNext();)
    {
      if(++number == 27) {
        break;
      }
      Entry<ByteString,Long> witnessInfo = it.next();
      Account request = Account.newBuilder().setAddress(witnessInfo.getKey()).build();
      logger.info("SR:" + number + blockingStubFull.getAccount(request).getAssetV2Count() + "");
      logger.info("SR:" + number + blockingStubFull.getAccount(request).getAssetV2Map().size() + "");
      logger.info("address:" + number + Base58.encode(witnessInfo.getKey().toByteArray()));
    }
    byte[] addressBytes = Wallet.decodeFromBase58Check("TLsV52sRDL79HXGGm9yzwKibb6BeruhUzy");
    ByteString addressBS = ByteString.copyFrom(addressBytes);
    Account request = Account.newBuilder().setAddress(addressBS).build();
    logger.info("blockhole:" + blockingStubFull.getAccount(request).getAssetV2Count());
    logger.info("blockhole:" + blockingStubFull.getAccount(request).getAssetV2Map().size());





  }

  private static class ValueComparator implements Comparator<Entry<ByteString,Long>>
  {
    public int compare(Map.Entry<ByteString,Long> m,Map.Entry<ByteString,Long> n)
    {
      return (int)(n.getValue()-m.getValue());
    }
  }

  @Test(enabled = false)
  public void test015DispatchTokenToSR() throws Exception {
    String foundAccountKey = "dc51f31e4de187c1c2530d65fb8f2958dff4c37f8cea430ce98d254baae37564";
    byte[] foundAccountAddress = PublicMethed.getFinalAddress(foundAccountKey);
    Integer tokenNumber = 600;
    while (tokenNumber-- >= 0) {
      ECKey ecKey1 = new ECKey(Utils.getRandom());
      byte[] tokenAddress = ecKey1.getAddress();
      String tokenKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
      //PublicMethed.printAddress(tokenKey);


      try {
        PublicMethed.sendcoin(tokenAddress,1030000000L,foundAccountAddress,foundAccountKey,blockingStubFull);
        Thread.sleep(20);
        Long start = System.currentTimeMillis() + 5000L;
        Long end = System.currentTimeMillis() + 5000000L;
        PublicMethed.createAssetIssue(tokenAddress, start+"id", 100000000000000L, 1,
            1, start, end, 1, "11", "11", 10000L, 10000L,
            1L, 1L, tokenKey, blockingStubFull);
        Thread.sleep(20);
      } catch (Exception e) {
        continue;
      }

      Account getAssetIdFromThisAccount = PublicMethed.queryAccount(tokenAddress, blockingStubFull);
      ByteString assetAccountId = getAssetIdFromThisAccount.getAssetIssuedID();

      String[] witnessList = {
          "541a2d585fcea7e9b1803df4eb49af0eb09f1fa2ce06aa5b8ed60ac95655d66d",
          "7d5a7396d6430edb7f66aa5736ef388f2bea862c9259de8ad8c2cfe080f6f5a0",
          "7c4977817417495f4ca0c35ab3d5a25e247355d68f89f593f3fea2ab62c8644f",
          "4521c13f65cc9f5c1daa56923b8598d4015801ad28379675c64106f5f6afec30",
          "f33101ea976d90491dcb9669be568db8bbc1ad23d90be4dede094976b67d550e",
          "1bb32958909299db452d3c9bbfd15fd745160d63e4985357874ee57708435a00",
          "29c91bd8b27c807d8dc2d2991aa0fbeafe7f54f4de9fac1e1684aa57242e3922",
          "97317d4d68a0c5ce14e74ad04dfc7521f142f5c0f247b632c8f94c755bdbe669",
          "1fe1d91bbe3ac4ac5dc9866c157ef7615ec248e3fd4f7d2b49b0428da5e046b2",
          "7c37ef485e186e07952bcc8e30cd911a6cd9f2a847736c89132762fb67a42329",
          "bcc142d57d872cd2cc1235bca454f2efd5a87f612856c979cc5b45a7399272a8",
          "6054824dc03546f903a06da1f405e72409379b83395d0bbb3d4563f56e828d52",
          "87cc8832b1b4860c3c69994bbfcdae9b520e6ce40cbe2a90566e707a7e04fc70",
          "c96c92c8a5f68ffba2ced3f7cd4baa6b784838a366f62914efdc79c6c18cd7d0",
          "d29e34899a21dc801c2be88184bed29a66246b5d85f26e8c77922ee2403a1934",
          "dc51f31e4de187c1c2530d65fb8f2958dff4c37f8cea430ce98d254baae37564",
          "ff43b371d67439bb8b6fa6c4ff615c954682008343d4cb2583b19f50adbac90f",
          "dbc78781ad27f3751358333412d5edc85b13e5eee129a1a77f7232baadafae0e",
          "a79a37a3d868e66456d76b233cb894d664b75fd91861340f3843db05ab3a8c66",
          "a8107ea1c97c90cd4d84e79cd79d327def6362cc6fd498fc3d3766a6a71924f6",
          "b5076206430b2ca069ae2f4dc6f20dd0d74551559878990d1df12a723c228039",
          "442513e2e801bc42d14d33b8148851dae756d08eeb48881a44e1b2002b3fb700"
      };
      for(String witnessKey : witnessList) {
        byte[] witnessAddress = PublicMethed.getFinalAddress(witnessKey);
        PublicMethed.transferAsset(witnessAddress,assetAccountId.toByteArray(),1L,tokenAddress,tokenKey,blockingStubFull);
        Thread.sleep(5);
        if(tokenNumber % 20 == 0) {
          Account request = Account.newBuilder().setAddress(ByteString.copyFrom(witnessAddress)).build();
          logger.info("SR token number:" + blockingStubFull.getAccount(request).getAssetV2Map().size());
        }
      }
      logger.info("number:" + tokenNumber);




    }

  }



  @Test(enabled = true)
  public void test016DeployJustlinkSmartContract() {
    // deployJst
    String contractName = "Jst";
    String abi = "[{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"stop\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"burn\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"stopped\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"start\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_name\",\"type\":\"string\"}],\"name\":\"setName\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"_addressFounder\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"}]";
    String code = "60c0604052600360808190527f4a7374000000000000000000000000000000000000000000000000000000000060a090815261003e9160009190610158565b506040805180820190915260078082527f4a73747465737400000000000000000000000000000000000000000000000000602090920191825261008391600191610158565b506012600255600060055560068054600160a860020a03191690553480156100aa57600080fd5b50d380156100b757600080fd5b50d280156100c457600080fd5b50604051602080610c4483398101604081815291516006805461010060a860020a031916336101000217905569152d02c7e14af68000006005819055600160a060020a03821660008181526003602090815286822084905592855294519294909390927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a3506101f3565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061019957805160ff19168380011785556101c6565b828001600101855582156101c6579182015b828111156101c65782518255916020019190600101906101ab565b506101d29291506101d6565b5090565b6101f091905b808211156101d257600081556001016101dc565b90565b610a42806102026000396000f3006080604052600436106100cf5763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166306fdde0381146100d457806307da68f514610178578063095ea7b3146101a957806318160ddd146101fb57806323b872dd1461023c578063313ce5671461028057806342966c68146102af57806370a08231146102e157806375f12b211461031c57806395d89b411461034b578063a9059cbb1461037a578063be9a6555146103b8578063c47f0027146103e7578063dd62ed3e1461045a575b600080fd5b3480156100e057600080fd5b50d380156100ed57600080fd5b50d280156100fa57600080fd5b5061010361049b565b6040805160208082528351818301528351919283929083019185019080838360005b8381101561013d578181015183820152602001610125565b50505050905090810190601f16801561016a5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561018457600080fd5b50d3801561019157600080fd5b50d2801561019e57600080fd5b506101a7610529565b005b3480156101b557600080fd5b50d380156101c257600080fd5b50d280156101cf57600080fd5b506101e7600160a060020a0360043516602435610551565b604080519115158252519081900360200190f35b34801561020757600080fd5b50d3801561021457600080fd5b50d2801561022157600080fd5b5061022a61060a565b60408051918252519081900360200190f35b34801561024857600080fd5b50d3801561025557600080fd5b50d2801561026257600080fd5b506101e7600160a060020a0360043581169060243516604435610610565b34801561028c57600080fd5b50d3801561029957600080fd5b50d280156102a657600080fd5b5061022a61072d565b3480156102bb57600080fd5b50d380156102c857600080fd5b50d280156102d557600080fd5b506101a7600435610733565b3480156102ed57600080fd5b50d380156102fa57600080fd5b50d2801561030757600080fd5b5061022a600160a060020a03600435166107ca565b34801561032857600080fd5b50d3801561033557600080fd5b50d2801561034257600080fd5b506101e76107dc565b34801561035757600080fd5b50d3801561036457600080fd5b50d2801561037157600080fd5b506101036107e5565b34801561038657600080fd5b50d3801561039357600080fd5b50d280156103a057600080fd5b506101e7600160a060020a036004351660243561083f565b3480156103c457600080fd5b50d380156103d157600080fd5b50d280156103de57600080fd5b506101a7610909565b3480156103f357600080fd5b50d3801561040057600080fd5b50d2801561040d57600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526101a794369492936024939284019190819084018382808284375094975061092e9650505050505050565b34801561046657600080fd5b50d3801561047357600080fd5b50d2801561048057600080fd5b5061022a600160a060020a036004358116906024351661095e565b6000805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156105215780601f106104f657610100808354040283529160200191610521565b820191906000526020600020905b81548152906001019060200180831161050457829003601f168201915b505050505081565b6006546101009004600160a060020a0316331461054257fe5b6006805460ff19166001179055565b60065460009060ff161561056157fe5b33151561056a57fe5b8115806105985750336000908152600460209081526040808320600160a060020a0387168452909152902054155b15156105a357600080fd5b336000818152600460209081526040808320600160a060020a03881680855290835292819020869055805186815290519293927f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925929181900390910190a350600192915050565b60055481565b60065460009060ff161561062057fe5b33151561062957fe5b600160a060020a03841660009081526003602052604090205482111561064e57600080fd5b600160a060020a038316600090815260036020526040902054828101101561067557600080fd5b600160a060020a03841660009081526004602090815260408083203384529091529020548211156106a557600080fd5b600160a060020a03808416600081815260036020908152604080832080548801905593881680835284832080548890039055600482528483203384528252918490208054879003905583518681529351929391927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9281900390910190a35060019392505050565b60025481565b3360009081526003602052604090205481111561074f57600080fd5b336000818152600360209081526040808320805486900390558280527f3617319a054d772f909f7c479a2cebe5066e836a939412e32403c99029b92eff805486019055805185815290519293927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a350565b60036020526000908152604090205481565b60065460ff1681565b60018054604080516020600284861615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156105215780601f106104f657610100808354040283529160200191610521565b60065460009060ff161561084f57fe5b33151561085857fe5b3360009081526003602052604090205482111561087457600080fd5b600160a060020a038316600090815260036020526040902054828101101561089b57600080fd5b33600081815260036020908152604080832080548790039055600160a060020a03871680845292819020805487019055805186815290519293927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a350600192915050565b6006546101009004600160a060020a0316331461092257fe5b6006805460ff19169055565b6006546101009004600160a060020a0316331461094757fe5b805161095a90600090602084019061097b565b5050565b600460209081526000928352604080842090915290825290205481565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106109bc57805160ff19168380011785556109e9565b828001600101855582156109e9579182015b828111156109e95782518255916020019190600101906109ce565b506109f59291506109f9565b5090565b610a1391905b808211156109f557600081556001016109ff565b905600a165627a7a72305820ebc9f6342ace7e43bd43d69616ace8c08dd29bf5af196e19b39ad0cc94ffeecc0029";
    String constructorStr = "constructor(address)";
    String data = "\"" + Base58.encode58Check(PublicMethed.getFinalAddress(triggerOwnerKey)) + "\"";
    String txid = PublicMethed.deployContractWithConstantParame(contractName, abi, code, constructorStr, data, "",
        1000000000L, 0L, 100, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> info = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    byte[] jstContractAddress = info.get().getContractAddress().toByteArray();
    logger.info("jstContractAddress " + WalletClient.encode58Check(jstContractAddress));

    // deployJustmid
    contractName = "JustMid";
    data = "\"" + Base58.encode58Check(jstContractAddress) + "\"";
    abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"from\",\"type\":\"address\"},{\"name\":\"to\",\"type\":\"address\"},{\"name\":\"tokens\",\"type\":\"uint256\"},{\"name\":\"_data\",\"type\":\"bytes\"}],\"name\":\"transferAndCall\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokenAddress\",\"type\":\"address\"}],\"name\":\"setToken\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getToken\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"from\",\"type\":\"address\"},{\"name\":\"to\",\"type\":\"address\"},{\"name\":\"tokens\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"guy\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"src\",\"type\":\"address\"},{\"name\":\"guy\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_newOwner\",\"type\":\"address\"}],\"name\":\"transferOwnership\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"_link\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"value\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"data\",\"type\":\"bytes\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"previousOwner\",\"type\":\"address\"}],\"name\":\"OwnershipRenounced\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"previousOwner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"newOwner\",\"type\":\"address\"}],\"name\":\"OwnershipTransferred\",\"type\":\"event\"}]";
    code = "608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50604051602080610963833981016040525160008054600160a060020a0319908116331790915560018054600160a060020a03909316929091169190911790556108ea806100796000396000f30060806040526004361061008d5763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663091136b48114610092578063144fa6d71461012f57806321df0da71461016a57806323b872dd146101b557806370a08231146101f95780638da5cb5b14610246578063dd62ed3e14610275578063f2fde38b146102b6575b600080fd5b34801561009e57600080fd5b50d380156100ab57600080fd5b50d280156100b857600080fd5b50604080516020601f60643560048181013592830184900484028501840190955281845261011b94600160a060020a0381358116956024803590921695604435953695608494019181908401838280828437509497506102f39650505050505050565b604080519115158252519081900360200190f35b34801561013b57600080fd5b50d3801561014857600080fd5b50d2801561015557600080fd5b5061011b600160a060020a036004351661049d565b34801561017657600080fd5b50d3801561018357600080fd5b50d2801561019057600080fd5b506101996104e5565b60408051600160a060020a039092168252519081900360200190f35b3480156101c157600080fd5b50d380156101ce57600080fd5b50d280156101db57600080fd5b5061011b600160a060020a03600435811690602435166044356104f4565b34801561020557600080fd5b50d3801561021257600080fd5b50d2801561021f57600080fd5b50610234600160a060020a03600435166105d1565b60408051918252519081900360200190f35b34801561025257600080fd5b50d3801561025f57600080fd5b50d2801561026c57600080fd5b5061019961066e565b34801561028157600080fd5b50d3801561028e57600080fd5b50d2801561029b57600080fd5b50610234600160a060020a036004358116906024351661067d565b3480156102c257600080fd5b50d380156102cf57600080fd5b50d280156102dc57600080fd5b506102f1600160a060020a0360043516610723565b005b600083600160a060020a038116158015906103175750600160a060020a0381163014155b151561032257600080fd5b600154604080517f23b872dd000000000000000000000000000000000000000000000000000000008152600160a060020a038981166004830152888116602483015260448201889052915191909216916323b872dd9160648083019260209291908290030181600087803b15801561039957600080fd5b505af11580156103ad573d6000803e3d6000fd5b505050506040513d60208110156103c357600080fd5b50506040805185815260208181018381528651938301939093528551600160a060020a03808a1694908b16937fe19260aff97b920c7df27010903aeb9c8d2be5d310a2c67824cf3f15396e4c16938a938a939160608401919085019080838360005b8381101561043d578181015183820152602001610425565b50505050905090810190601f16801561046a5780820380516001836020036101000a031916815260200191505b50935050505060405180910390a361048185610746565b156104915761049185858561074e565b50600195945050505050565b60008054600160a060020a031633146104b557600080fd5b5060018054600160a060020a03831673ffffffffffffffffffffffffffffffffffffffff19909116178155919050565b600154600160a060020a031690565b600082600160a060020a038116158015906105185750600160a060020a0381163014155b151561052357600080fd5b600154604080517f23b872dd000000000000000000000000000000000000000000000000000000008152600160a060020a038881166004830152878116602483015260448201879052915191909216916323b872dd9160648083019260209291908290030181600087803b15801561059a57600080fd5b505af11580156105ae573d6000803e3d6000fd5b505050506040513d60208110156105c457600080fd5b5060019695505050505050565b600154604080517f70a08231000000000000000000000000000000000000000000000000000000008152600160a060020a038481166004830152915160009392909216916370a082319160248082019260209290919082900301818787803b15801561063c57600080fd5b505af1158015610650573d6000803e3d6000fd5b505050506040513d602081101561066657600080fd5b505192915050565b600054600160a060020a031681565b600154604080517fdd62ed3e000000000000000000000000000000000000000000000000000000008152600160a060020a03858116600483015284811660248301529151600093929092169163dd62ed3e9160448082019260209290919082900301818787803b1580156106f057600080fd5b505af1158015610704573d6000803e3d6000fd5b505050506040513d602081101561071a57600080fd5b50519392505050565b600054600160a060020a0316331461073a57600080fd5b61074381610841565b50565b6000903b1190565b6040517fa4c0ed360000000000000000000000000000000000000000000000000000000081523360048201818152602483018590526060604484019081528451606485015284518794600160a060020a0386169463a4c0ed369490938993899360840190602085019080838360005b838110156107d55781810151838201526020016107bd565b50505050905090810190601f1680156108025780820380516001836020036101000a031916815260200191505b50945050505050600060405180830381600087803b15801561082357600080fd5b505af1158015610837573d6000803e3d6000fd5b5050505050505050565b600160a060020a038116151561085657600080fd5b60008054604051600160a060020a03808516939216917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e091a36000805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a03929092169190911790555600a165627a7a7230582006c6d6857a4e8a0e270c19675cf5a773c3a967c7008d21a1323123459f7e61b30029";
    txid = PublicMethed.deployContractWithConstantParame(contractName, abi, code, constructorStr, data, "",
        1000000000L, 0L, 100, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    info = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    byte[] jstMidContractAddress = info.get().getContractAddress().toByteArray();
    logger.info("jstMidContractAddress " + WalletClient.encode58Check(jstMidContractAddress));

    // deployOracles
    contractName = "Oracle";
    constructorStr = "constructor(address,address)";
    data = "\"" + Base58.encode58Check(jstContractAddress) + "\",\"" + Base58
        .encode58Check(jstMidContractAddress) + "\"";
    abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"_sender\",\"type\":\"address\"},{\"name\":\"_payment\",\"type\":\"uint256\"},{\"name\":\"_specId\",\"type\":\"bytes32\"},{\"name\":\"_callbackAddress\",\"type\":\"address\"},{\"name\":\"_callbackFunctionId\",\"type\":\"bytes4\"},{\"name\":\"_nonce\",\"type\":\"uint256\"},{\"name\":\"_dataVersion\",\"type\":\"uint256\"},{\"name\":\"_data\",\"type\":\"bytes\"}],\"name\":\"oracleRequest\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_requestId\",\"type\":\"bytes32\"},{\"name\":\"_payment\",\"type\":\"uint256\"},{\"name\":\"_callbackAddress\",\"type\":\"address\"},{\"name\":\"_callbackFunctionId\",\"type\":\"bytes4\"},{\"name\":\"_expiration\",\"type\":\"uint256\"},{\"name\":\"_data\",\"type\":\"bytes32\"}],\"name\":\"fulfillOracleRequest\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"EXPIRY_TIME\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"withdrawable\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"justMidAddress\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_requestId\",\"type\":\"bytes32\"},{\"name\":\"_payment\",\"type\":\"uint256\"},{\"name\":\"_callbackFunc\",\"type\":\"bytes4\"},{\"name\":\"_expiration\",\"type\":\"uint256\"}],\"name\":\"cancelOracleRequest\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_node\",\"type\":\"address\"},{\"name\":\"_allowed\",\"type\":\"bool\"}],\"name\":\"setFulfillmentPermission\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_sender\",\"type\":\"address\"},{\"name\":\"_amount\",\"type\":\"uint256\"},{\"name\":\"_data\",\"type\":\"bytes\"}],\"name\":\"onTokenTransfer\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_node\",\"type\":\"address\"}],\"name\":\"getAuthorizationStatus\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_newOwner\",\"type\":\"address\"}],\"name\":\"transferOwnership\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_recipient\",\"type\":\"address\"},{\"name\":\"_amount\",\"type\":\"uint256\"}],\"name\":\"withdraw\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"_link\",\"type\":\"address\"},{\"name\":\"_justMid\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"specId\",\"type\":\"bytes32\"},{\"indexed\":false,\"name\":\"requester\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"requestId\",\"type\":\"bytes32\"},{\"indexed\":false,\"name\":\"payment\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"callbackAddr\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"callbackFunctionId\",\"type\":\"bytes4\"},{\"indexed\":false,\"name\":\"cancelExpiration\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"dataVersion\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"data\",\"type\":\"bytes\"}],\"name\":\"OracleRequest\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"requestId\",\"type\":\"bytes32\"}],\"name\":\"CancelOracleRequest\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"previousOwner\",\"type\":\"address\"}],\"name\":\"OwnershipRenounced\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"previousOwner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"newOwner\",\"type\":\"address\"}],\"name\":\"OwnershipTransferred\",\"type\":\"event\"}]";
    code = "6080604052600160055534801561001557600080fd5b50d3801561002257600080fd5b50d2801561002f57600080fd5b506040516040806113c483398101604052805160209091015160008054600160a060020a0319908116331790915560028054600160a060020a0394851690831617905560018054939092169216919091179055611333806100916000396000f3006080604052600436106100a05763ffffffff60e060020a6000350416634042994681146100a55780634ab0d190146101125780634b6022821461017a57806350188301146101bb5780636ab9f92d146101ea5780636ee4d553146102355780637fcd56db1461027a5780638da5cb5b146102ba578063a4c0ed36146102e9578063d3e9c3141461036c578063f2fde38b146103a7578063f3fef3a3146103e2575b600080fd5b3480156100b157600080fd5b50d380156100be57600080fd5b50d280156100cb57600080fd5b50610110600160a060020a03600480358216916024803592604435926064351691600160e060020a0319608435169160a4359160c4359160e435918201910135610420565b005b34801561011e57600080fd5b50d3801561012b57600080fd5b50d2801561013857600080fd5b50610166600435602435600160a060020a0360443516600160e060020a03196064351660843560a4356107e4565b604080519115158252519081900360200190f35b34801561018657600080fd5b50d3801561019357600080fd5b50d280156101a057600080fd5b506101a9610a80565b60408051918252519081900360200190f35b3480156101c757600080fd5b50d380156101d457600080fd5b50d280156101e157600080fd5b506101a9610a86565b3480156101f657600080fd5b50d3801561020357600080fd5b50d2801561021057600080fd5b50610219610ab7565b60408051600160a060020a039092168252519081900360200190f35b34801561024157600080fd5b50d3801561024e57600080fd5b50d2801561025b57600080fd5b50610110600435602435600160e060020a031960443516606435610ac6565b34801561028657600080fd5b50d3801561029357600080fd5b50d280156102a057600080fd5b50610110600160a060020a03600435166024351515610daa565b3480156102c657600080fd5b50d380156102d357600080fd5b50d280156102e057600080fd5b50610219610dec565b3480156102f557600080fd5b50d3801561030257600080fd5b50d2801561030f57600080fd5b50604080516020600460443581810135601f8101849004840285018401909552848452610110948235600160a060020a0316946024803595369594606494920191908190840183828082843750949750610dfb9650505050505050565b34801561037857600080fd5b50d3801561038557600080fd5b50d2801561039257600080fd5b50610166600160a060020a0360043516611020565b3480156103b357600080fd5b50d380156103c057600080fd5b50d280156103cd57600080fd5b50610110600160a060020a036004351661103e565b3480156103ee57600080fd5b50d380156103fb57600080fd5b50d2801561040857600080fd5b50610110600160a060020a0360043516602435611061565b6001546000908190600160a060020a03163314610487576040805160e560020a62461bcd02815260206004820152601060248201527f4d75737420757365204a7573744d696400000000000000000000000000000000604482015290519081900360640190fd5b6001548890600160a060020a03808316911614156104ef576040805160e560020a62461bcd02815260206004820152601760248201527f43616e6e6f742063616c6c6261636b20746f204c494e4b000000000000000000604482015290519081900360640190fd5b8b876040516020018083600160a060020a0316600160a060020a03166c01000000000000000000000000028152601401828152602001925050506040516020818303038152906040526040518082805190602001908083835b602083106105675780518252601f199092019160209182019101610548565b51815160209384036101000a600019018019909216911617905260408051929094018290039091206000818152600390925292902054919650501591506105fa9050576040805160e560020a62461bcd02815260206004820152601460248201527f4d75737420757365206120756e69717565204944000000000000000000000000604482015290519081900360640190fd5b61060c4261012c63ffffffff61126516565b6040805160208082018f90526c01000000000000000000000000600160a060020a038e160282840152600160e060020a03198c1660548301526058808301859052835180840390910181526078909201928390528151939550909282918401908083835b6020831061068f5780518252601f199092019160209182019101610670565b6001836020036101000a0380198251168184511680821785525050505050509050019150506040518091039020600360008560001916600019168152602001908152602001600020816000191690555089600019167fd8d7ecc4800d25fa53ce0372f13a416d98907a7ef3d8d3bdd79cf4fe75529c658d858e8d8d888d8d8d604051808a600160a060020a0316600160a060020a03168152602001896000191660001916815260200188815260200187600160a060020a0316600160a060020a03168152602001867bffffffffffffffffffffffffffffffffffffffffffffffffffffffff19167bffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916815260200185815260200184815260200180602001828103825284848281815260200192508082843760405192018290039c50909a5050505050505050505050a2505050505050505050505050565b33600090815260046020526040812054819060ff168061080e5750600054600160a060020a031633145b151561088a576040805160e560020a62461bcd02815260206004820152602a60248201527f4e6f7420616e20617574686f72697a6564206e6f646520746f2066756c66696c60448201527f6c20726571756573747300000000000000000000000000000000000000000000606482015290519081900360840190fd5b600088815260036020526040902054889015156108f1576040805160e560020a62461bcd02815260206004820152601b60248201527f4d757374206861766520612076616c6964207265717565737449640000000000604482015290519081900360640190fd5b6040805160208082018b90526c01000000000000000000000000600160a060020a038b160282840152600160e060020a031989166054830152605880830189905283518084039091018152607890920192839052815191929182918401908083835b602083106109725780518252601f199092019160209182019101610953565b51815160209384036101000a6000190180199092169116179052604080519290940182900390912060008f81526003909252929020549195505084149150610a069050576040805160e560020a62461bcd02815260206004820152601e60248201527f506172616d7320646f206e6f74206d6174636820726571756573742049440000604482015290519081900360640190fd5b600554610a19908963ffffffff61126516565b600555600089815260036020526040808220829055805163ffffffff60e060020a808b04918216028252600482018d9052602482018890529151600160a060020a038b1693604480840193919291829003018183875af19c9b505050505050505050505050565b61012c81565b60008054600160a060020a03163314610a9e57600080fd5b600554610ab290600163ffffffff61127816565b905090565b600154600160a060020a031690565b6040805160208082018690526c01000000000000000000000000330282840152600160e060020a0319851660548301526058808301859052835180840390910181526078909201928390528151600093918291908401908083835b60208310610b405780518252601f199092019160209182019101610b21565b51815160209384036101000a6000190180199092169116179052604080519290940182900390912060008b81526003909252929020549194505083149150610bd49050576040805160e560020a62461bcd02815260206004820152601e60248201527f506172616d7320646f206e6f74206d6174636820726571756573742049440000604482015290519081900360640190fd5b42821115610c2c576040805160e560020a62461bcd02815260206004820152601660248201527f52657175657374206973206e6f74206578706972656400000000000000000000604482015290519081900360640190fd5b6000858152600360205260408082208290555186917fa7842b9ec549398102c0d91b1b9919b2f20558aefdadf57528a95c6cd3292e9391a2600254600160a060020a031663095ea7b3610c7d610ab7565b866040518363ffffffff1660e060020a0281526004018083600160a060020a0316600160a060020a0316815260200182815260200192505050602060405180830381600087803b158015610cd057600080fd5b505af1158015610ce4573d6000803e3d6000fd5b505050506040513d6020811015610cfa57600080fd5b5050600154604080517f23b872dd000000000000000000000000000000000000000000000000000000008152306004820152336024820152604481018790529051600160a060020a03909216916323b872dd916064808201926020929091908290030181600087803b158015610d6f57600080fd5b505af1158015610d83573d6000803e3d6000fd5b505050506040513d6020811015610d9957600080fd5b50511515610da357fe5b5050505050565b600054600160a060020a03163314610dc157600080fd5b600160a060020a03919091166000908152600460205260409020805460ff1916911515919091179055565b600054600160a060020a031681565b600154600160a060020a03163314610e5d576040805160e560020a62461bcd02815260206004820152601060248201527f4d75737420757365204a7573744d696400000000000000000000000000000000604482015290519081900360640190fd5b8051819060441115610eb9576040805160e560020a62461bcd02815260206004820152601660248201527f496e76616c69642072657175657374206c656e67746800000000000000000000604482015290519081900360640190fd5b60208201518290600160e060020a031981167f404299460000000000000000000000000000000000000000000000000000000014610f41576040805160e560020a62461bcd02815260206004820152601e60248201527f4d757374207573652077686974656c69737465642066756e6374696f6e730000604482015290519081900360640190fd5b85602485015284604485015230600160a060020a03168460405180828051906020019080838360005b83811015610f82578181015183820152602001610f6a565b50505050905090810190601f168015610faf5780820380516001836020036101000a031916815260200191505b50915050600060405180830381855af49150501515611018576040805160e560020a62461bcd02815260206004820152601860248201527f556e61626c6520746f2063726561746520726571756573740000000000000000604482015290519081900360640190fd5b505050505050565b600160a060020a031660009081526004602052604090205460ff1690565b600054600160a060020a0316331461105557600080fd5b61105e8161128a565b50565b600054600160a060020a0316331461107857600080fd5b8061108a81600163ffffffff61126516565b6005541015611109576040805160e560020a62461bcd02815260206004820152603560248201527f416d6f756e74207265717565737465642069732067726561746572207468616e60448201527f20776974686472617761626c652062616c616e63650000000000000000000000606482015290519081900360840190fd5b60055461111c908363ffffffff61127816565b600555600254600160a060020a031663095ea7b3611138610ab7565b846040518363ffffffff1660e060020a0281526004018083600160a060020a0316600160a060020a0316815260200182815260200192505050602060405180830381600087803b15801561118b57600080fd5b505af115801561119f573d6000803e3d6000fd5b505050506040513d60208110156111b557600080fd5b5050600154604080517f23b872dd000000000000000000000000000000000000000000000000000000008152306004820152600160a060020a03868116602483015260448201869052915191909216916323b872dd9160648083019260209291908290030181600087803b15801561122c57600080fd5b505af1158015611240573d6000803e3d6000fd5b505050506040513d602081101561125657600080fd5b5051151561126057fe5b505050565b8181018281101561127257fe5b92915050565b60008282111561128457fe5b50900390565b600160a060020a038116151561129f57600080fd5b60008054604051600160a060020a03808516939216917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e091a36000805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a03929092169190911790555600a165627a7a72305820bc158c17520b9c48243bf1f85b1d39a51390b2250edc02858268b508db215b9f0029";
    List<String> oracleContractAddressList = new ArrayList<String>();
    for (int i = 0; i < 7; i++) {
      txid = PublicMethed
          .deployContractWithConstantParame(contractName, abi, code, constructorStr, data, "",
              1000000000L, 0L, 100, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      info = PublicMethed
          .getTransactionInfoById(txid, blockingStubFull);
      String oracleContractAddress = Base58
          .encode58Check(info.get().getContractAddress().toByteArray());
      Assert.assertNotNull(oracleContractAddress);
      oracleContractAddressList.add(oracleContractAddress);
    }

    // deployAgg
    contractName = "Aggregator";
    data = "\"" + Base58.encode58Check(jstContractAddress) + "\",\"" + Base58
        .encode58Check(jstMidContractAddress) + "\"";
    abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"_requestId\",\"type\":\"bytes32\"},{\"name\":\"_payment\",\"type\":\"uint256\"},{\"name\":\"_expiration\",\"type\":\"uint256\"}],\"name\":\"cancelRequest\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_clRequestId\",\"type\":\"bytes32\"},{\"name\":\"_response\",\"type\":\"int256\"}],\"name\":\"justlinkCallback\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"authorizedRequesters\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"jobIds\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"latestAnswer\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"minimumResponses\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"oracles\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_recipient\",\"type\":\"address\"},{\"name\":\"_amount\",\"type\":\"uint256\"}],\"name\":\"transferLINK\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"latestRound\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"justMidAddress\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_paymentAmount\",\"type\":\"uint128\"},{\"name\":\"_minimumResponses\",\"type\":\"uint128\"},{\"name\":\"_oracles\",\"type\":\"address[]\"},{\"name\":\"_jobIds\",\"type\":\"bytes32[]\"}],\"name\":\"updateRequestDetails\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"latestTimestamp\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"destroy\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_roundId\",\"type\":\"uint256\"}],\"name\":\"getAnswer\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_roundId\",\"type\":\"uint256\"}],\"name\":\"getTimestamp\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"paymentAmount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"requestRateUpdate\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getOracleSize\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_requester\",\"type\":\"address\"},{\"name\":\"_allowed\",\"type\":\"bool\"}],\"name\":\"setAuthorization\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_newOwner\",\"type\":\"address\"}],\"name\":\"transferOwnership\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"_link\",\"type\":\"address\"},{\"name\":\"_justMid\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"response\",\"type\":\"int256\"},{\"indexed\":true,\"name\":\"answerId\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"sender\",\"type\":\"address\"}],\"name\":\"ResponseReceived\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"previousOwner\",\"type\":\"address\"}],\"name\":\"OwnershipRenounced\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"previousOwner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"newOwner\",\"type\":\"address\"}],\"name\":\"OwnershipTransferred\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"id\",\"type\":\"bytes32\"}],\"name\":\"JustlinkRequested\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"id\",\"type\":\"bytes32\"}],\"name\":\"JustlinkFulfilled\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"id\",\"type\":\"bytes32\"}],\"name\":\"JustlinkCancelled\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"current\",\"type\":\"int256\"},{\"indexed\":true,\"name\":\"roundId\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"timestamp\",\"type\":\"uint256\"}],\"name\":\"AnswerUpdated\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"roundId\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"startedBy\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"startedAt\",\"type\":\"uint256\"}],\"name\":\"NewRound\",\"type\":\"event\"}]";
    code = "608060405260016003556001600c553480156200001b57600080fd5b50d380156200002957600080fd5b50d280156200003757600080fd5b506040516040806200201983398101604052805160209091015160058054600160a060020a03191633179055620000778264010000000062000093810204565b6200008b81640100000000620000b5810204565b5050620000d7565b60018054600160a060020a031916600160a060020a0392909216919091179055565b60008054600160a060020a031916600160a060020a0392909216919091179055565b611f3280620000e76000396000f3006080604052600436106101035763ffffffff60e060020a60003504166333bfcdd8811461010857806337031fc6146101425780633ea478aa146101775780634162cc88146101c657806350d25bcd1461020a57806354bcd7ff146102395780635b69a7d8146102845780635cd9b90b146102d2578063668a0f02146103105780636ab9f92d1461033f57806378a666741461036e5780638205bf6a1461042c57806383197ef01461045b5780638da5cb5b1461048a578063b5ab58dc146104b9578063b633620c146104eb578063c35905c61461051d578063daa6d5561461054c578063ed72eb2414610554578063eecea00014610583578063f2fde38b146105c3575b600080fd5b34801561011457600080fd5b50d3801561012157600080fd5b50d2801561012e57600080fd5b506101406004356024356044356105fe565b005b34801561014e57600080fd5b50d3801561015b57600080fd5b50d2801561016857600080fd5b5061014060043560243561079c565b34801561018357600080fd5b50d3801561019057600080fd5b50d2801561019d57600080fd5b506101b2600160a060020a0360043516610824565b604080519115158252519081900360200190f35b3480156101d257600080fd5b50d380156101df57600080fd5b50d280156101ec57600080fd5b506101f8600435610839565b60408051918252519081900360200190f35b34801561021657600080fd5b50d3801561022357600080fd5b50d2801561023057600080fd5b506101f8610858565b34801561024557600080fd5b50d3801561025257600080fd5b50d2801561025f57600080fd5b5061026861086e565b604080516001608060020a039092168252519081900360200190f35b34801561029057600080fd5b50d3801561029d57600080fd5b50d280156102aa57600080fd5b506102b6600435610891565b60408051600160a060020a039092168252519081900360200190f35b3480156102de57600080fd5b50d380156102eb57600080fd5b50d280156102f857600080fd5b50610140600160a060020a03600435166024356108b9565b34801561031c57600080fd5b50d3801561032957600080fd5b50d2801561033657600080fd5b506101f8610a66565b34801561034b57600080fd5b50d3801561035857600080fd5b50d2801561036557600080fd5b506102b6610a6c565b34801561037a57600080fd5b50d3801561038757600080fd5b50d2801561039457600080fd5b5060408051602060046044358181013583810280860185019096528085526101409583356001608060020a039081169660248035909216963696956064959294930192829185019084908082843750506040805187358901803560208181028481018201909552818452989b9a998901989297509082019550935083925085019084908082843750949750610a7b9650505050505050565b34801561043857600080fd5b50d3801561044557600080fd5b50d2801561045257600080fd5b506101f8610c6b565b34801561046757600080fd5b50d3801561047457600080fd5b50d2801561048157600080fd5b50610140610c80565b34801561049657600080fd5b50d380156104a357600080fd5b50d280156104b057600080fd5b506102b6610d48565b3480156104c557600080fd5b50d380156104d257600080fd5b50d280156104df57600080fd5b506101f8600435610d57565b3480156104f757600080fd5b50d3801561050457600080fd5b50d2801561051157600080fd5b506101f8600435610d69565b34801561052957600080fd5b50d3801561053657600080fd5b50d2801561054357600080fd5b50610268610d7b565b6101f8610d8a565b34801561056057600080fd5b50d3801561056d57600080fd5b50d2801561057a57600080fd5b506101f8610ff1565b34801561058f57600080fd5b50d3801561059c57600080fd5b50d280156105a957600080fd5b50610140600160a060020a03600435166024351515610ff7565b3480156105cf57600080fd5b50d380156105dc57600080fd5b50d280156105e957600080fd5b50610140600160a060020a0360043516611039565b336000908152600d602052604081205460ff16806106265750600554600160a060020a031633145b15156106a2576040805160e560020a62461bcd02815260206004820152602f60248201527f4e6f7420616e20617574686f72697a6564206164647265737320666f7220637260448201527f656174696e672072657175657374730000000000000000000000000000000000606482015290519081900360840190fd5b506000838152600e60205260409020546008548110610731576040805160e560020a62461bcd02815260206004820152602360248201527f43616e6e6f74206d6f6469667920616e20696e2d70726f677265737320616e7360448201527f7765720000000000000000000000000000000000000000000000000000000000606482015290519081900360840190fd5b6000848152600e60209081526040808320839055838352600f825282206001908101805491820181558352908220015561076a8161105c565b61079684847f37031fc600000000000000000000000000000000000000000000000000000000856110b6565b50505050565b60006107a7836111b4565b506000828152600e60209081526040808320805490849055808452600f835281842060019081018054918201815585529284209092018490555190913391839185917fb51168059c83c860caf5b830c5d2e64c2172c6fb2fe9f25447d9838e18d93b609190a46108168161129e565b61081f8161105c565b505050565b600d6020526000908152604090205460ff1681565b600a80548290811061084757fe5b600091825260209091200154905081565b6008546000908152601060205260409020545b90565b60095470010000000000000000000000000000000090046001608060020a031681565b600b80548290811061089f57fe5b600091825260209091200154600160a060020a0316905081565b600554600160a060020a031633146108d057600080fd5b600154600160a060020a031663095ea7b36108e9610a6c565b836040518363ffffffff1660e060020a0281526004018083600160a060020a0316600160a060020a0316815260200182815260200192505050602060405180830381600087803b15801561093c57600080fd5b505af1158015610950573d6000803e3d6000fd5b505050506040513d602081101561096657600080fd5b505060008054604080517f23b872dd000000000000000000000000000000000000000000000000000000008152306004820152600160a060020a03868116602483015260448201869052915191909216926323b872dd92606480820193602093909283900390910190829087803b1580156109e057600080fd5b505af11580156109f4573d6000803e3d6000fd5b505050506040513d6020811015610a0a57600080fd5b50511515610a62576040805160e560020a62461bcd02815260206004820152601460248201527f4c494e4b207472616e73666572206661696c6564000000000000000000000000604482015290519081900360640190fd5b5050565b60085490565b600054600160a060020a031690565b600554600160a060020a03163314610a9257600080fd5b826001608060020a03168282601c825111151515610afa576040805160e560020a62461bcd02815260206004820181905260248201527f63616e6e6f742068617665206d6f7265207468616e203435206f7261636c6573604482015290519081900360640190fd5b8151831115610b79576040805160e560020a62461bcd02815260206004820152602f60248201527f6d7573742068617665206174206c65617374206173206d616e79206f7261636c60448201527f657320617320726573706f6e7365730000000000000000000000000000000000606482015290519081900360840190fd5b8051825114610bf8576040805160e560020a62461bcd02815260206004820152602c60248201527f6d75737420686176652065786163746c79206173206d616e79206f7261636c6560448201527f73206173206a6f62204944730000000000000000000000000000000000000000606482015290519081900360840190fd5b600980546001608060020a0388811670010000000000000000000000000000000002818b166fffffffffffffffffffffffffffffffff1990931692909217161790558351610c4d90600a906020870190611d95565b508451610c6190600b906020880190611de2565b5050505050505050565b60085460009081526011602052604090205490565b600554600160a060020a03163314610c9757600080fd5b60055460008054604080517f70a082310000000000000000000000000000000000000000000000000000000081523060048201529051610d3a94600160a060020a03908116949316926370a0823192602480820193602093909283900390910190829087803b158015610d0957600080fd5b505af1158015610d1d573d6000803e3d6000fd5b505050506040513d6020811015610d3357600080fd5b50516108b9565b600554600160a060020a0316ff5b600554600160a060020a031681565b60009081526010602052604090205490565b60009081526011602052604090205490565b6009546001608060020a031681565b6000610d94611e50565b336000908152600d60205260408120548190819060ff1680610dc05750600554600160a060020a031633145b1515610e3c576040805160e560020a62461bcd02815260206004820152602f60248201527f4e6f7420616e20617574686f72697a6564206164647265737320666f7220637260448201527f656174696e672072657175657374730000000000000000000000000000000000606482015290519081900360840190fd5b600b54600010610e96576040805160e560020a62461bcd02815260206004820152601d60248201527f506c6561736520736574206f7261636c657320616e64206a6f62496473000000604482015290519081900360640190fd5b50506009546001608060020a031660005b600b54811015610f2b57610edf600a82815481101515610ec357fe5b9060005260206000200154306337031fc660e060020a026114f1565b9350610f0f600b82815481101515610ef357fe5b600091825260209091200154600160a060020a0316858461151c565b600c546000828152600e60205260409020559250600101610ea7565b600954600c80546000908152600f6020908152604080832080546fffffffffffffffffffffffffffffffff19166001608060020a0370010000000000000000000000000000000097889004811691909117909155600b5485548552938290208054948216909602931692909217909355905481514281529151339391927f0109fc6f55cf40689f02fbaad7af7fe7bbac8a3d2186600afc7d3e10cac6027192908290030190a3600c54610fe590600163ffffffff61186b16565b600c5550909392505050565b600b5490565b600554600160a060020a0316331461100e57600080fd5b600160a060020a03919091166000908152600d60205260409020805460ff1916911515919091179055565b600554600160a060020a0316331461105057600080fd5b6110598161187e565b50565b6000818152600f602052604090208054600190910154829170010000000000000000000000000000000090046001608060020a03161415610a62576000828152600f60205260408120818155906107966001830182611e85565b600084815260046020526040808220805473ffffffffffffffffffffffffffffffffffffffff1981169091559051600160a060020a039091169186917f9a167888573a46ab8b19fc20c1d700b4142a92b1b77a75d257385310561ca5759190a2604080517f6ee4d5530000000000000000000000000000000000000000000000000000000081526004810187905260248101869052600160e060020a031985166044820152606481018490529051600160a060020a03831691636ee4d55391608480830192600092919082900301818387803b15801561119557600080fd5b505af11580156111a9573d6000803e3d6000fd5b505050505050505050565b6000818152600460205260409020548190600160a060020a0316331461124a576040805160e560020a62461bcd02815260206004820152602860248201527f536f75726365206d75737420626520746865206f7261636c65206f662074686560448201527f2072657175657374000000000000000000000000000000000000000000000000606482015290519081900360840190fd5b600081815260046020526040808220805473ffffffffffffffffffffffffffffffffffffffff191690555182917fe8f77abd86335d681cca28f3e94bfffcb22d9491bc788ca98cf92d6052c2f3d991a25050565b6000818152600f602052604081208054600190910154829182918291829187916001608060020a0316116114e8578680600854111515610c61576000888152600f602052604090206001015496506112fd87600263ffffffff6118fc16565b955060028706151561140f576000888152600f6020908152604091829020600101805483518184028101840190945280845261136e939283018282801561136357602002820191906000526020600020905b81548152602001906001019080831161134f575b505050505087611911565b6000898152600f602090815260409182902060010180548351818402810184019094528084529397506113ea9390918301828280156113cc57602002820191906000526020600020905b8154815260200190600101908083116113b8575b50505050506113e560018961186b90919063ffffffff16565b611911565b925060026113fe858563ffffffff611b0516565b81151561140757fe5b059450611483565b6000888152600f6020908152604091829020600101805483518184028101840190945280845261148093928301828280156113cc57602002820191906000526020600020908154815260200190600101908083116113b85750505050506113e560018961186b90919063ffffffff16565b94505b6006859055600888905542600781905560008981526011602090815260408083208490556010825291829020889055815192835290518a9288927f0559884fd3a460db3073b7fc896cc77986f16e378210ded43186175bf646fc5f92918290030190a3505b50505050505050565b6114f9611e50565b611501611e50565b6115138186868663ffffffff611bb216565b95945050505050565b6000306003546040516020018083600160a060020a0316600160a060020a03166c01000000000000000000000000028152601401828152602001925050506040516020818303038152906040526040518082805190602001908083835b602083106115985780518252601f199092019160209182019101611579565b51815160209384036101000a6000190180199092169116179052604080519290940182900390912060035460608a015260008181526004909252838220805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a038c1617905592519295508594507f1c76b7fd125284eb547b13385d3b876d50158862c12ecd61f37f466f415711209350919050a2600154600160a060020a031663095ea7b3611643610a6c565b846040518363ffffffff1660e060020a0281526004018083600160a060020a0316600160a060020a0316815260200182815260200192505050602060405180830381600087803b15801561169657600080fd5b505af11580156116aa573d6000803e3d6000fd5b505050506040513d60208110156116c057600080fd5b5050600054600160a060020a031663091136b43086856116df88611bef565b6040518563ffffffff1660e060020a0281526004018085600160a060020a0316600160a060020a0316815260200184600160a060020a0316600160a060020a0316815260200183815260200180602001828103825283818151815260200191508051906020019080838360005b8381101561176457818101518382015260200161174c565b50505050905090810190601f1680156117915780820380516001836020036101000a031916815260200191505b5095505050505050602060405180830381600087803b1580156117b357600080fd5b505af11580156117c7573d6000803e3d6000fd5b505050506040513d60208110156117dd57600080fd5b5051151561185b576040805160e560020a62461bcd02815260206004820152602360248201527f756e61626c6520746f207472616e73666572416e6443616c6c20746f206f726160448201527f636c650000000000000000000000000000000000000000000000000000000000606482015290519081900360840190fd5b6003805460010190559392505050565b8181018281101561187857fe5b92915050565b600160a060020a038116151561189357600080fd5b600554604051600160a060020a038084169216907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e090600090a36005805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a0392909216919091179055565b6000818381151561190957fe5b049392505050565b600060606000806060806000806000808b98508a97508851965086604051908082528060200260200182016040528015611955578160200160208202803883390190505b50955086604051908082528060200260200182016040528015611982578160200160208202803883390190505b5094505b8861199888600263ffffffff6118fc16565b815181106119a257fe5b9060200190602002015191506000935060009250600090505b86811015611a7b578189828151811015156119d257fe5b906020019060200201511215611a1d5788818151811015156119f057fe5b906020019060200201518685815181101515611a0857fe5b60209081029091010152600190930192611a73565b818982815181101515611a2c57fe5b906020019060200201511315611a73578881815181101515611a4a57fe5b906020019060200201518584815181101515611a6257fe5b602090810290910101526001909201915b6001016119bb565b838811611a9957839650611a8f8987611d46565b9099509550611af1565b611aa9878463ffffffff611d4916565b881115611ae957611ad0611ac3888563ffffffff611d4916565b899063ffffffff611d4916565b9750829650611adf8986611d46565b9099509450611af1565b819950611af6565b611986565b50505050505050505092915050565b6000828201818312801590611b1a5750838112155b80611b2f5750600083128015611b2f57508381125b1515611bab576040805160e560020a62461bcd02815260206004820152602160248201527f5369676e6564536166654d6174683a206164646974696f6e206f766572666c6f60448201527f7700000000000000000000000000000000000000000000000000000000000000606482015290519081900360840190fd5b9392505050565b611bba611e50565b611bca8560800151610100611d5b565b5050918352600160a060020a03166020830152600160e060020a031916604082015290565b8051602080830151604080850151606086810151608088015151935160006024820181815260448301829052606483018a9052600160a060020a0388166084840152600160e060020a0319861660a484015260c48301849052600160e48401819052610100610104850190815288516101248601528851969b7f40429946000000000000000000000000000000000000000000000000000000009b949a8b9a91999098909796939591949361014401918501908083838e5b83811015611cbf578181015183820152602001611ca7565b50505050905090810190601f168015611cec5780820380516001836020036101000a031916815260200191505b5060408051601f198184030181529190526020810180517bffffffffffffffffffffffffffffffffffffffffffffffffffffffff16600160e060020a0319909d169c909c17909b5250989950505050505050505050919050565b91565b600082821115611d5557fe5b50900390565b611d63611ea3565b6020820615611d785760208206602003820191505b506020828101829052604080518085526000815290920101905290565b828054828255906000526020600020908101928215611dd2579160200282015b82811115611dd25782518255602090920191600190910190611db5565b50611dde929150611ebb565b5090565b828054828255906000526020600020908101928215611e44579160200282015b82811115611e44578251825473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a03909116178255602090920191600190910190611e02565b50611dde929150611ed5565b6040805160c081018252600080825260208201819052918101829052606081019190915260808101611e80611ea3565b905290565b50805460008255906000526020600020908101906110599190611ebb565b60408051808201909152606081526000602082015290565b61086b91905b80821115611dde5760008155600101611ec1565b61086b91905b80821115611dde57805473ffffffffffffffffffffffffffffffffffffffff19168155600101611edb5600a165627a7a723058204dabd0ed24ea9010aa95c3ab9bad4c0fd5e31aba02d40b30a48e7a7cf2388c0e0029";
    txid = PublicMethed.deployContractWithConstantParame(contractName, abi, code, constructorStr, data, "",
        1000000000L, 0L, 100, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    info = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    byte[] aggContractAddress = info.get().getContractAddress().toByteArray();
    newContractAddress = WalletClient.encode58Check(aggContractAddress);
    oldAddress = readWantedText("stress.conf", "aggContractAddress");
    newAddress = "  aggContractAddress = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    // updateRequestDetails
    String methodStr = "updateRequestDetails(uint128,uint128,address[],bytes32[])";
    String oracleAddressParam = "[";
    for (int i = 0; i < oracleContractAddressList.size(); i++) {
      if (i == 0) {
        oracleAddressParam += "\"" + oracleContractAddressList.get(i) + "\"";
      } else {
        oracleAddressParam += ",\"" + oracleContractAddressList.get(i) + "\"";
      }
    }
    oracleAddressParam += "],";
    data = "\"1\","
        + "\"7\","
        + oracleAddressParam
        + "[\"bb347a9a63324fd995a7159cb0c8348a\",\"40691f5fd4b64ab4a5442477ed484d80\",\"f7ccb652cc254a19b0b954c49af25926\",\"38cd68072a6c4a0ca05e9b91976cf4f1\",\"328697ef599043e1a301ae985d06aabf\",\"239ff4228974435ea33f7c32cb46d297\",\"10ee483bad154f41ac58fdb4010c2c63\"]";
    logger.info("data:"+data);
    txid = PublicMethed
        .triggerContract(aggContractAddress, methodStr, data, false, 0, 1000000000L,
            PublicMethed.getFinalAddress(triggerOwnerKey), triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    // transferJst
    data = "\"" + Base58.encode58Check(aggContractAddress) + "\"," + 5000000000000000000l;
    txid = PublicMethed
        .triggerContract(jstContractAddress, "transfer(address,uint256)", data, false,
            0, 1000000000L, PublicMethed.getFinalAddress(triggerOwnerKey), triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test017DeployJustSwapSmartContract() {
    // deployJustswapFactory
    String contractName = "JustswapFactory";
    String abi = "[{\"constant\":true,\"inputs\":[{\"name\":\"token\",\"type\":\"address\"}],\"name\":\"getExchange\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"token\",\"type\":\"address\"}],\"name\":\"createExchange\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"exchangeTemplate\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"template\",\"type\":\"address\"}],\"name\":\"initializeFactory\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"exchange\",\"type\":\"address\"}],\"name\":\"getToken\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"tokenCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"token_id\",\"type\":\"uint256\"}],\"name\":\"getTokenWithId\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"token\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"exchange\",\"type\":\"address\"}],\"name\":\"NewExchange\",\"type\":\"event\"}]";
    String code = "608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b506144178061003a6000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50600436106100975760003560e01c8063538a3f0e11610075578063538a3f0e1461010c57806359770438146101345780639f181b5e1461015a578063aa65a6c01461017457610097565b806306f2bf621461009c5780631648f38e146100de5780631c2bbd1814610104575b600080fd5b6100c2600480360360208110156100b257600080fd5b50356001600160a01b0316610191565b604080516001600160a01b039092168252519081900360200190f35b6100c2600480360360208110156100f457600080fd5b50356001600160a01b03166101af565b6100c26103f4565b6101326004803603602081101561012257600080fd5b50356001600160a01b0316610403565b005b6100c26004803603602081101561014a57600080fd5b50356001600160a01b03166104d1565b6101626104ef565b60408051918252519081900360200190f35b6100c26004803603602081101561018a57600080fd5b50356104f5565b6001600160a01b039081166000908152600260205260409020541690565b60006001600160a01b0382166101fc576040805162461bcd60e51b815260206004820152600d60248201526c34b63632b3b0b6103a37b5b2b760991b604482015290519081900360640190fd5b6000546001600160a01b0316610259576040805162461bcd60e51b815260206004820152601860248201527f65786368616e676554656d706c617465206e6f74207365740000000000000000604482015290519081900360640190fd5b6001600160a01b0382811660009081526002602052604090205416156102c6576040805162461bcd60e51b815260206004820152601860248201527f65786368616e676520616c726561647920637265617465640000000000000000604482015290519081900360640190fd5b60006040516102d490610510565b604051809103906000f0801580156102f0573d6000803e3d6000fd5b509050806001600160a01b03166366d38203846040518263ffffffff1660e01b815260040180826001600160a01b03166001600160a01b03168152602001915050600060405180830381600087803b15801561034b57600080fd5b505af115801561035f573d6000803e3d6000fd5b5050506001600160a01b03808516600081815260026020908152604080832080549588166001600160a01b0319968716811790915580845260038352818420805487168617905560018054810190819055808552600490935281842080549096168517909555519094507f9d42cb017eb05bd8944ab536a8b35bc68085931dd5f4356489801453923953f99190a35092915050565b6000546001600160a01b031681565b6000546001600160a01b031615610461576040805162461bcd60e51b815260206004820152601c60248201527f65786368616e676554656d706c61746520616c72656164792073657400000000604482015290519081900360640190fd5b6001600160a01b0381166104af576040805162461bcd60e51b815260206004820152601060248201526f696c6c6567616c2074656d706c61746560801b604482015290519081900360640190fd5b600080546001600160a01b0319166001600160a01b0392909216919091179055565b6001600160a01b039081166000908152600360205260409020541690565b60015481565b6000908152600460205260409020546001600160a01b031690565b613ec68061051e8339019056fe60806040526003805460ff19166001179055613ea6806100206000396000f3fe6080604052600436106102255760003560e01c8063999bb7ac11610123578063ce22f858116100ab578063f396b84c1161006f578063f396b84c14610c35578063f3c0efe914610c79578063f552d91b14610ce6578063f88bf15a14610d53578063fd11c22314610dc257610225565b8063ce22f85814610a9e578063dd62ed3e14610ad0578063ddf7e1a714610b25578063ea650c7d14610b63578063ec384a3e14610bc857610225565b8063a7c6d9d9116100f2578063a7c6d9d91461091a578063a8f85ca61461095e578063a9059cbb14610981578063b040d545146109d4578063b1cb43bf14610a3957610225565b8063999bb7ac146107f857806399a13409146108485780639d76ea5814610898578063a457c2d7146108c757610225565b80634bf3e2d0116101b15780639378b0e8116101755780639378b0e81461068957806395d89b41146106cd578063966dae0e146106fc57806397430b4e14610747578063981a13271461078b57610225565b80634bf3e2d01461054857806366d382031461056b57806370a08231146105ba57806387f1ab151461060757806389f2a8711461063957610225565b806318160ddd116101f857806318160ddd1461041157806323b872dd14610440578063313ce5671461049d57806339509351146104cc578063422f10431461051f57610225565b80630127f1421461023657806306fdde03146102a7578063095ea7b31461034b57806314178dda146103b2575b610233346001423333610e12565b50005b34801561024257600080fd5b50d3801561024f57600080fd5b50d2801561025c57600080fd5b506102956004803603608081101561027357600080fd5b50803590602081013590604081013590606001356001600160a01b031661110f565b60408051918252519081900360200190f35b3480156102b357600080fd5b50d380156102c057600080fd5b50d280156102cd57600080fd5b506102d6611151565b6040805160208082528351818301528351919283929083019185019080838360005b838110156103105781810151838201526020016102f8565b50505050905090810190601f16801561033d5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561035757600080fd5b50d3801561036457600080fd5b50d2801561037157600080fd5b5061039e6004803603604081101561038857600080fd5b506001600160a01b0381351690602001356111df565b604080519115158252519081900360200190f35b3480156103be57600080fd5b50d380156103cb57600080fd5b50d280156103d857600080fd5b50610295600480360360808110156103ef57600080fd5b50803590602081013590604081013590606001356001600160a01b03166111f6565b34801561041d57600080fd5b50d3801561042a57600080fd5b50d2801561043757600080fd5b5061029561122f565b34801561044c57600080fd5b50d3801561045957600080fd5b50d2801561046657600080fd5b5061039e6004803603606081101561047d57600080fd5b506001600160a01b03813581169160208101359091169060400135611236565b3480156104a957600080fd5b50d380156104b657600080fd5b50d280156104c357600080fd5b5061029561128e565b3480156104d857600080fd5b50d380156104e557600080fd5b50d280156104f257600080fd5b5061039e6004803603604081101561050957600080fd5b506001600160a01b038135169060200135611294565b6102956004803603606081101561053557600080fd5b50803590602081013590604001356112d0565b6102956004803603604081101561055e57600080fd5b508035906020013561194d565b34801561057757600080fd5b50d3801561058457600080fd5b50d2801561059157600080fd5b506105b8600480360360208110156105a857600080fd5b50356001600160a01b031661195c565b005b3480156105c657600080fd5b50d380156105d357600080fd5b50d280156105e057600080fd5b50610295600480360360208110156105f757600080fd5b50356001600160a01b0316611a6b565b6102956004803603606081101561061d57600080fd5b50803590602081013590604001356001600160a01b0316611a86565b34801561064557600080fd5b50d3801561065257600080fd5b50d2801561065f57600080fd5b506102956004803603606081101561067657600080fd5b5080359060208101359060400135611ac7565b34801561069557600080fd5b50d380156106a257600080fd5b50d280156106af57600080fd5b50610295600480360360208110156106c657600080fd5b5035611b78565b3480156106d957600080fd5b50d380156106e657600080fd5b50d280156106f357600080fd5b506102d6611c55565b34801561070857600080fd5b50d3801561071557600080fd5b50d2801561072257600080fd5b5061072b611cb0565b604080516001600160a01b039092168252519081900360200190f35b34801561075357600080fd5b50d3801561076057600080fd5b50d2801561076d57600080fd5b506102956004803603602081101561078457600080fd5b5035611cbf565b34801561079757600080fd5b50d380156107a457600080fd5b50d280156107b157600080fd5b50610295600480360360c08110156107c857600080fd5b508035906020810135906040810135906060810135906001600160a01b03608082013581169160a0013516611d9a565b34801561080457600080fd5b50d3801561081157600080fd5b50d2801561081e57600080fd5b506102956004803603606081101561083557600080fd5b5080359060208101359060400135611dfd565b34801561085457600080fd5b50d3801561086157600080fd5b50d2801561086e57600080fd5b506102956004803603606081101561088557600080fd5b5080359060208101359060400135611e0c565b3480156108a457600080fd5b50d380156108b157600080fd5b50d280156108be57600080fd5b5061072b611e1b565b3480156108d357600080fd5b50d380156108e057600080fd5b50d280156108ed57600080fd5b5061039e6004803603604081101561090457600080fd5b506001600160a01b038135169060200135611e2a565b34801561092657600080fd5b50d3801561093357600080fd5b50d2801561094057600080fd5b506102956004803603602081101561095757600080fd5b5035611e66565b6102956004803603604081101561097457600080fd5b5080359060200135611f2d565b34801561098d57600080fd5b50d3801561099a57600080fd5b50d280156109a757600080fd5b5061039e600480360360408110156109be57600080fd5b506001600160a01b038135169060200135611f3c565b3480156109e057600080fd5b50d380156109ed57600080fd5b50d280156109fa57600080fd5b50610295600480360360a0811015610a1157600080fd5b50803590602081013590604081013590606081013590608001356001600160a01b0316611f49565b348015610a4557600080fd5b50d38015610a5257600080fd5b50d28015610a5f57600080fd5b50610295600480360360a0811015610a7657600080fd5b50803590602081013590604081013590606081013590608001356001600160a01b0316611fd5565b61029560048036036060811015610ab457600080fd5b50803590602081013590604001356001600160a01b0316611ff0565b348015610adc57600080fd5b50d38015610ae957600080fd5b50d28015610af657600080fd5b5061029560048036036040811015610b0d57600080fd5b506001600160a01b0381358116916020013516612029565b610295600480360360a0811015610b3b57600080fd5b50803590602081013590604081013590606081013590608001356001600160a01b0316612054565b348015610b6f57600080fd5b50d38015610b7c57600080fd5b50d28015610b8957600080fd5b50610295600480360360a0811015610ba057600080fd5b50803590602081013590604081013590606081013590608001356001600160a01b03166120e0565b348015610bd457600080fd5b50d38015610be157600080fd5b50d28015610bee57600080fd5b50610295600480360360c0811015610c0557600080fd5b508035906020810135906040810135906060810135906001600160a01b03608082013581169160a00135166120f1565b348015610c4157600080fd5b50d38015610c4e57600080fd5b50d28015610c5b57600080fd5b5061029560048036036020811015610c7257600080fd5b5035612154565b348015610c8557600080fd5b50d38015610c9257600080fd5b50d28015610c9f57600080fd5b50610295600480360360c0811015610cb657600080fd5b508035906020810135906040810135906060810135906001600160a01b03608082013581169160a001351661222f565b348015610cf257600080fd5b50d38015610cff57600080fd5b50d28015610d0c57600080fd5b50610295600480360360c0811015610d2357600080fd5b508035906020810135906040810135906060810135906001600160a01b03608082013581169160a00135166122c7565b348015610d5f57600080fd5b50d38015610d6c57600080fd5b50d28015610d7957600080fd5b50610da960048036036080811015610d9057600080fd5b5080359060208101359060408101359060600135612353565b6040805192835260208301919091528051918290030190f35b348015610dce57600080fd5b50d38015610ddb57600080fd5b50d28015610de857600080fd5b5061029560048036036060811015610dff57600080fd5b5080359060208101359060400135612742565b60035460009060ff16610e5a576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff19169055428410801590610e745750600086115b8015610e805750600085115b610ed1576040805162461bcd60e51b815260206004820152601760248201527f747278546f546f6b656e496e7075742d72657175697265000000000000000000604482015290519081900360640190fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b158015610f1c57600080fd5b505afa158015610f30573d6000803e3d6000fd5b505050506040513d6020811015610f4657600080fd5b505190506000610f6788610f6130318263ffffffff6127b116565b84611ac7565b905086811015610fbe576040805162461bcd60e51b815260206004820152601860248201527f746f6b656e735f626f756768743c6d696e5f746f6b656e730000000000000000604482015290519081900360640190fd5b600754610fdb906001600160a01b0316858363ffffffff6127f316565b611022576040805162461bcd60e51b81526020600482015260136024820152721cd85999551c985b9cd9995c8819985a5b1959606a1b604482015290519081900360640190fd5b8088866001600160a01b03167fcd60aa75dea3072fbc07ae6d7d856b5dc5f4eee88854f5b4abf7b680ef8bc50f60405160405180910390a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b1580156110a557600080fd5b505afa1580156110b9573d6000803e3d6000fd5b505050506040513d60208110156110cf57600080fd5b50516040513031906001600160a01b03881690600080516020613d8883398151915290600090a49150506003805460ff1916600117905595945050505050565b60006001600160a01b038216301480159061113257506001600160a01b03821615155b61113b57600080fd5b611148858585338661293e565b95945050505050565b6004805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156111d75780601f106111ac576101008083540402835291602001916111d7565b820191906000526020600020905b8154815290600101906020018083116111ba57829003601f168201915b505050505081565b60006111ec338484612b86565b5060015b92915050565b60006001600160a01b038216301480159061121957506001600160a01b03821615155b61122257600080fd5b6111488585853386612c0e565b6002545b90565b6000611243848484612dde565b6001600160a01b03841660009081526001602090815260408083203380855292529091205461128391869161127e908663ffffffff6127b116565b612b86565b5060015b9392505050565b60065481565b3360008181526001602090815260408083206001600160a01b038716845290915281205490916111ec91859061127e908663ffffffff612e9716565b60035460009060ff16611318576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff1916905542821180156113315750600083115b801561133d5750600034115b6113785760405162461bcd60e51b815260040180806020018281038252602b815260200180613da8602b913960400191505060405180910390fd5b600254801561168257600085116113c05760405162461bcd60e51b8152600401808060200182810382526021815260200180613d676021913960400191505060405180910390fd5b60006113d330313463ffffffff6127b116565b600754604080516370a0823160e01b815230600482015290519293506000926001600160a01b03909216916370a0823191602480820192602092909190829003018186803b15801561142457600080fd5b505afa158015611438573d6000803e3d6000fd5b505050506040513d602081101561144e57600080fd5b50519050600061148560016114798561146d348763ffffffff612ef116565b9063ffffffff612f4a16565b9063ffffffff612e9716565b9050600061149d8461146d348863ffffffff612ef116565b90508188101580156114af5750888110155b6114ea5760405162461bcd60e51b815260040180806020018281038252603e815260200180613dd3603e913960400191505060405180910390fd5b3360009081526020819052604090205461150a908263ffffffff612e9716565b3360009081526020819052604090205561152a858263ffffffff612e9716565b60025560075461154b906001600160a01b031633308563ffffffff612f8c16565b61158e576040805162461bcd60e51b815260206004820152600f60248201526e1d1c985b9cd9995c8819985a5b1959608a1b604482015290519081900360640190fd5b6040518290349033907f06239653922ac7bea6aa2b19dc486b9361821d37712eb796adfd38d81de278ca90600090a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b15801561160857600080fd5b505afa15801561161c573d6000803e3d6000fd5b505050506040513d602081101561163257600080fd5b50516040513031903390600080516020613d8883398151915290600090a46040805182815290513391600091600080516020613e328339815191529181900360200190a394506119399350505050565b6008546001600160a01b0316158015906116a657506007546001600160a01b031615155b80156116b55750629896803410155b6116f6576040805162461bcd60e51b815260206004820152600d60248201526c494e56414c49445f56414c554560981b604482015290519081900360640190fd5b600854600754604080516303795fb160e11b81526001600160a01b03928316600482015290513093909216916306f2bf6291602480820192602092909190829003018186803b15801561174857600080fd5b505afa15801561175c573d6000803e3d6000fd5b505050506040513d602081101561177257600080fd5b50516001600160a01b0316146117cf576040805162461bcd60e51b815260206004820152601f60248201527f746f6b656e2061646472657373206e6f74206d6565742065786368616e676500604482015290519081900360640190fd5b30803160028190553360008181526020819052604090208290556007548793611805926001600160a01b03909216919085612f8c565b611847576040805162461bcd60e51b815260206004820152600e60248201526d1d1c985b99995c8819985a5b195960921b604482015290519081900360640190fd5b6040518290349033907f06239653922ac7bea6aa2b19dc486b9361821d37712eb796adfd38d81de278ca90600090a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b1580156118c157600080fd5b505afa1580156118d5573d6000803e3d6000fd5b505050506040513d60208110156118eb57600080fd5b50516040513031903390600080516020613d8883398151915290600090a46040805182815290513391600091600080516020613e328339815191529181900360200190a39250611939915050565b6003805460ff191660011790559392505050565b60006112873484843333610e12565b6008546001600160a01b031615801561197e57506007546001600160a01b0316155b801561199257506001600160a01b03811615155b6119d5576040805162461bcd60e51b815260206004820152600f60248201526e494e56414c49445f4144445245535360881b604482015290519081900360640190fd5b60088054336001600160a01b031991821617909155600780549091166001600160a01b03831617905560408051808201909152600b8082526a4a7573747377617020563160a81b6020909201918252611a3091600491613c6b565b5060408051808201909152600b8082526a4a555354535741502d563160a81b6020909201918252611a6391600591613c6b565b505060068055565b6001600160a01b031660009081526020819052604090205490565b60006001600160a01b0382163014801590611aa957506001600160a01b03821615155b611ab257600080fd5b611abf84348533866130b2565b949350505050565b60008083118015611ad85750600082115b611b19576040805162461bcd60e51b815260206004820152600d60248201526c494e56414c49445f56414c554560981b604482015290519081900360640190fd5b6000611b2d856103e563ffffffff612ef116565b90506000611b41828563ffffffff612ef116565b90506000611b5b83611479886103e863ffffffff612ef116565b9050611b6d828263ffffffff612f4a16565b979650505050505050565b6000808211611bce576040805162461bcd60e51b815260206004820152601f60248201527f746f6b656e7320736f6c64206d7573742067726561746572207468616e203000604482015290519081900360640190fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b158015611c1957600080fd5b505afa158015611c2d573d6000803e3d6000fd5b505050506040513d6020811015611c4357600080fd5b505190506000611abf84833031611ac7565b6005805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156111d75780601f106111ac576101008083540402835291602001916111d7565b6008546001600160a01b031690565b6000808211611d15576040805162461bcd60e51b815260206004820152601c60248201527f74727820736f6c64206d7573742067726561746572207468616e203000000000604482015290519081900360640190fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b158015611d6057600080fd5b505afa158015611d74573d6000803e3d6000fd5b505050506040513d6020811015611d8a57600080fd5b5051905061128783303183611ac7565b60006001600160a01b038316301415611dee576040805162461bcd60e51b81526020600482015260116024820152701a5b1b1959d85b081c9958da5c1a595b9d607a1b604482015290519081900360640190fd5b611b6d87878787338888613327565b6000611abf848484333361293e565b6000611abf8484843333612c0e565b6007546001600160a01b031690565b3360008181526001602090815260408083206001600160a01b038716845290915281205490916111ec91859061127e908663ffffffff6127b116565b6000808211611ea65760405162461bcd60e51b8152600401808060200182810382526021815260200180613e526021913960400191505060405180910390fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b158015611ef157600080fd5b505afa158015611f05573d6000803e3d6000fd5b505050506040513d6020811015611f1b57600080fd5b505190506000611abf84303184612742565b600061128783348433336130b2565b60006111ec338484612dde565b600854604080516303795fb160e11b81526001600160a01b0384811660048301529151600093849316916306f2bf62916024808301926020929190829003018186803b158015611f9857600080fd5b505afa158015611fac573d6000803e3d6000fd5b505050506040513d6020811015611fc257600080fd5b50519050611b6d87878787338087613327565b6000611fe686868686333388613786565b9695505050505050565b60006001600160a01b038216301480159061201357506001600160a01b03821615155b61201c57600080fd5b611abf3485853386610e12565b6001600160a01b03918216600090815260016020908152604080832093909416825291909152205490565b600854604080516303795fb160e11b81526001600160a01b0384811660048301529151600093849316916306f2bf62916024808301926020929190829003018186803b1580156120a357600080fd5b505afa1580156120b7573d6000803e3d6000fd5b505050506040513d60208110156120cd57600080fd5b50519050611b6d87878787338087613786565b6000611fe686868686333388613327565b60006001600160a01b038316301415612145576040805162461bcd60e51b81526020600482015260116024820152701a5b1b1959d85b081c9958da5c1a595b9d607a1b604482015290519081900360640190fd5b611b6d87878787338888613786565b60008082116121aa576040805162461bcd60e51b815260206004820152601e60248201527f74727820626f75676874206d7573742067726561746572207468616e20300000604482015290519081900360640190fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b1580156121f557600080fd5b505afa158015612209573d6000803e3d6000fd5b505050506040513d602081101561221f57600080fd5b5051905061128783823031612742565b600854604080516303795fb160e11b81526001600160a01b0384811660048301529151600093849316916306f2bf62916024808301926020929190829003018186803b15801561227e57600080fd5b505afa158015612292573d6000803e3d6000fd5b505050506040513d60208110156122a857600080fd5b505190506122bb88888888338987613327565b98975050505050505050565b600854604080516303795fb160e11b81526001600160a01b0384811660048301529151600093849316916306f2bf62916024808301926020929190829003018186803b15801561231657600080fd5b505afa15801561232a573d6000803e3d6000fd5b505050506040513d602081101561234057600080fd5b505190506122bb88888888338987613786565b600354600090819060ff1661239d576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff1916905585158015906123b557504283115b80156123c15750600085115b80156123cd5750600084115b612419576040805162461bcd60e51b8152602060048201526018602482015277696c6c6567616c20696e70757420706172616d657465727360401b604482015290519081900360640190fd5b600254806124585760405162461bcd60e51b8152600401808060200182810382526023815260200180613d446023913960400191505060405180910390fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b1580156124a357600080fd5b505afa1580156124b7573d6000803e3d6000fd5b505050506040513d60208110156124cd57600080fd5b505190506000826124e58a303163ffffffff612ef116565b816124ec57fe5b0490506000836125028b8563ffffffff612ef116565b8161250957fe5b04905088821015801561251c5750878110155b61256d576040805162461bcd60e51b815260206004820152601d60248201527f6d696e5f746f6b656e206f72206d696e5f747278206e6f74206d656574000000604482015290519081900360640190fd5b3360009081526020819052604090205461258d908b63ffffffff6127b116565b336000908152602081905260409020556125ad848b63ffffffff6127b116565b600255604051339083156108fc029084906000818181858888f193505050501580156125dd573d6000803e3d6000fd5b506007546125fb906001600160a01b0316338363ffffffff6127f316565b61263e576040805162461bcd60e51b815260206004820152600f60248201526e1d1c985b9cd9995c8819985a5b1959608a1b604482015290519081900360640190fd5b6040518190839033907f0fbf06c058b90cb038a618f8c2acbf6145f8b3570fd1fa56abb8f0f3f05b36e890600090a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b1580156126b857600080fd5b505afa1580156126cc573d6000803e3d6000fd5b505050506040513d60208110156126e257600080fd5b50516040513031903390600080516020613d8883398151915290600090a4604080518b815290516000913391600080516020613e328339815191529181900360200190a36003805460ff1916600117905590999098509650505050505050565b600080831180156127535750600082115b61275c57600080fd5b60006127806103e8612774868863ffffffff612ef116565b9063ffffffff612ef116565b9050600061279a6103e5612774868963ffffffff6127b116565b9050611fe66001611479848463ffffffff612f4a16565b600061128783836040518060400160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f770000815250613b6f565b604080516001600160a01b038481166024830152604480830185905283518084039091018152606490920183526020820180516001600160e01b031663a9059cbb60e01b1781529251825160009485946060948a16939092909182918083835b602083106128725780518252601f199092019160209182019101612853565b6001836020036101000a0380198251168184511680821785525050505050509050019150506000604051808303816000865af19150503d80600081146128d4576040519150601f19603f3d011682016040523d82523d6000602084013e6128d9565b606091505b5090925090506001600160a01b03861673a614f803b6fd780986a42c78ec9c7f77e6ded13c141561290c57509050611287565b818015611fe6575080511580611fe6575080806020019051602081101561293257600080fd5b50519695505050505050565b60035460009060ff16612986576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff191690554284108015906129a05750600086115b80156129ac5750600085115b6129b557600080fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b158015612a0057600080fd5b505afa158015612a14573d6000803e3d6000fd5b505050506040513d6020811015612a2a57600080fd5b505190506000612a3c88833031611ac7565b90508087811015612a4c57600080fd5b6040516001600160a01b0386169082156108fc029083906000818181858888f19350505050158015612a82573d6000803e3d6000fd5b50600754612aa1906001600160a01b031687308c63ffffffff612f8c16565b612aaa57600080fd5b8089876001600160a01b0316600080516020613d0483398151915260405160405180910390a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b158015612b1b57600080fd5b505afa158015612b2f573d6000803e3d6000fd5b505050506040513d6020811015612b4557600080fd5b50516040513031906001600160a01b03891690600080516020613d8883398151915290600090a4925050506003805460ff1916600117905595945050505050565b6001600160a01b038216612b9957600080fd5b6001600160a01b038316612bac57600080fd5b6001600160a01b03808416600081815260016020908152604080832094871680845294825291829020859055815185815291517f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b9259281900390910190a3505050565b60035460009060ff16612c56576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff19169055428410801590612c705750600086115b612c7957600080fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b158015612cc457600080fd5b505afa158015612cd8573d6000803e3d6000fd5b505050506040513d6020811015612cee57600080fd5b505190506000612d0088833031612742565b905080871015612d0f57600080fd5b6040516001600160a01b0385169089156108fc02908a906000818181858888f19350505050158015612d45573d6000803e3d6000fd5b50600754612d64906001600160a01b031686308463ffffffff612f8c16565b612d6d57600080fd5b8781866001600160a01b0316600080516020613d0483398151915260405160405180910390a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b1580156110a557600080fd5b6001600160a01b038216612df157600080fd5b6001600160a01b038316600090815260208190526040902054612e1a908263ffffffff6127b116565b6001600160a01b038085166000908152602081905260408082209390935590841681522054612e4f908263ffffffff612e9716565b6001600160a01b03808416600081815260208181526040918290209490945580518581529051919392871692600080516020613e3283398151915292918290030190a3505050565b600082820183811015611287576040805162461bcd60e51b815260206004820152601b60248201527f536166654d6174683a206164646974696f6e206f766572666c6f770000000000604482015290519081900360640190fd5b600082612f00575060006111f0565b82820282848281612f0d57fe5b04146112875760405162461bcd60e51b8152600401808060200182810382526021815260200180613e116021913960400191505060405180910390fd5b600061128783836040518060400160405280601a81526020017f536166654d6174683a206469766973696f6e206279207a65726f000000000000815250613c06565b604080516001600160a01b0385811660248301528481166044830152606480830185905283518084039091018152608490920183526020820180516001600160e01b03166323b872dd60e01b1781529251825160009485946060948b16939092909182918083835b602083106130135780518252601f199092019160209182019101612ff4565b6001836020036101000a0380198251168184511680821785525050505050509050019150506000604051808303816000865af19150503d8060008114613075576040519150601f19603f3d011682016040523d82523d6000602084013e61307a565b606091505b5091509150818015611b6d575080511580611b6d57508080602001905160208110156130a557600080fd5b5051979650505050505050565b60035460009060ff166130fa576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff191690554284108015906131145750600086115b80156131205750600085115b61312957600080fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b15801561317457600080fd5b505afa158015613188573d6000803e3d6000fd5b505050506040513d602081101561319e57600080fd5b5051905060006131bf886131b930318a63ffffffff6127b116565b84612742565b905060006131d3888363ffffffff6127b116565b90508015613213576040516001600160a01b0387169082156108fc029083906000818181858888f19350505050158015613211573d6000803e3d6000fd5b505b600754613230906001600160a01b0316868b63ffffffff6127f316565b61323957600080fd5b8882876001600160a01b03167fcd60aa75dea3072fbc07ae6d7d856b5dc5f4eee88854f5b4abf7b680ef8bc50f60405160405180910390a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b1580156132bc57600080fd5b505afa1580156132d0573d6000803e3d6000fd5b505050506040513d60208110156132e657600080fd5b50516040513031906001600160a01b03891690600080516020613d8883398151915290600090a4509150506003805460ff1916600117905595945050505050565b60035460009060ff1661336f576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff1916905542851080159061339457506000881180156133945750600086115b6133e0576040805162461bcd60e51b8152602060048201526018602482015277696c6c6567616c20696e70757420706172616d657465727360401b604482015290519081900360640190fd5b6001600160a01b038216301480159061340157506001600160a01b03821615155b61344a576040805162461bcd60e51b815260206004820152601560248201527434b63632b3b0b61032bc31b430b733b29030b2323960591b604482015290519081900360640190fd5b6000826001600160a01b031663a7c6d9d98a6040518263ffffffff1660e01b81526004018082815260200191505060206040518083038186803b15801561349057600080fd5b505afa1580156134a4573d6000803e3d6000fd5b505050506040513d60208110156134ba57600080fd5b5051600754604080516370a0823160e01b815230600482015290519293506000926001600160a01b03909216916370a0823191602480820192602092909190829003018186803b15801561350d57600080fd5b505afa158015613521573d6000803e3d6000fd5b505050506040513d602081101561353757600080fd5b50519050600061354983833031612742565b9050808a1015801561355b5750828910155b6135ac576040805162461bcd60e51b815260206004820152601a60248201527f6d617820746f6b656e20736f6c64206e6f74206d617463686564000000000000604482015290519081900360640190fd5b6007546135ca906001600160a01b031688308463ffffffff612f8c16565b61360d576040805162461bcd60e51b815260206004820152600f60248201526e1d1c985b9cd9995c8819985a5b1959608a1b604482015290519081900360640190fd5b6000856001600160a01b03166387f1ab15858e8c8b6040518563ffffffff1660e01b815260040180848152602001838152602001826001600160a01b03166001600160a01b0316815260200193505050506020604051808303818588803b15801561367757600080fd5b505af115801561368b573d6000803e3d6000fd5b50505050506040513d60208110156136a257600080fd5b5051604051909150849083906001600160a01b038b1690600080516020613d0483398151915290600090a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b15801561371857600080fd5b505afa15801561372c573d6000803e3d6000fd5b505050506040513d602081101561374257600080fd5b50516040513031906001600160a01b038b1690600080516020613d8883398151915290600090a450925050506003805460ff19166001179055979650505050505050565b60035460009060ff166137ce576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff191690554285108015906137e85750600088115b80156137f45750600087115b80156138005750600086115b61384c576040805162461bcd60e51b8152602060048201526018602482015277696c6c6567616c20696e70757420706172616d657465727360401b604482015290519081900360640190fd5b6001600160a01b038216301480159061386d57506001600160a01b03821615155b6138b6576040805162461bcd60e51b815260206004820152601560248201527434b63632b3b0b61032bc31b430b733b29030b2323960591b604482015290519081900360640190fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b15801561390157600080fd5b505afa158015613915573d6000803e3d6000fd5b505050506040513d602081101561392b57600080fd5b50519050600061393d8a833031611ac7565b90508088811015613995576040805162461bcd60e51b815260206004820152601a60248201527f6d696e2074727820626f75676874206e6f74206d617463686564000000000000604482015290519081900360640190fd5b6007546139b3906001600160a01b031688308e63ffffffff612f8c16565b6139f6576040805162461bcd60e51b815260206004820152600f60248201526e1d1c985b9cd9995c8819985a5b1959608a1b604482015290519081900360640190fd5b6000856001600160a01b031663ce22f858838d8c8b6040518563ffffffff1660e01b815260040180848152602001838152602001826001600160a01b03166001600160a01b0316815260200193505050506020604051808303818588803b158015613a6057600080fd5b505af1158015613a74573d6000803e3d6000fd5b50505050506040513d6020811015613a8b57600080fd5b505160405190915082908d906001600160a01b038b1690600080516020613d0483398151915290600090a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b158015613b0157600080fd5b505afa158015613b15573d6000803e3d6000fd5b505050506040513d6020811015613b2b57600080fd5b50516040513031906001600160a01b038b1690600080516020613d8883398151915290600090a493505050506003805460ff19166001179055979650505050505050565b60008184841115613bfe5760405162461bcd60e51b81526004018080602001828103825283818151815260200191508051906020019080838360005b83811015613bc3578181015183820152602001613bab565b50505050905090810190601f168015613bf05780820380516001836020036101000a031916815260200191505b509250505060405180910390fd5b505050900390565b60008183613c555760405162461bcd60e51b8152602060048201818152835160248401528351909283926044909101919085019080838360008315613bc3578181015183820152602001613bab565b506000838581613c6157fe5b0495945050505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10613cac57805160ff1916838001178555613cd9565b82800160010185558215613cd9579182015b82811115613cd9578251825591602001919060010190613cbe565b50613ce5929150613ce9565b5090565b61123391905b80821115613ce55760008155600101613cef56fedad9ec5c9b9c82bf6927bf0b64293dcdd1f82c92793aef3c5f26d7b93a4a53065265656e7472616e637947756172643a207265656e7472616e742063616c6c00746f74616c5f6c6971756964697479206d7573742067726561746572207468616e20306d696e5f6c6971756964697479206d7573742067726561746572207468616e2030cc7244d3535e7639366f8c5211527112e01de3ec7449ee3a6e66b007f4065a704a75737445786368616e6765236164644c69717569646974793a20494e56414c49445f415247554d454e546d617820746f6b656e73206e6f74206d656574206f72206c69717569646974795f6d696e746564206e6f74206d656574206d696e5f6c6971756964697479536166654d6174683a206d756c7469706c69636174696f6e206f766572666c6f77ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef746f6b656e7320626f75676874206d7573742067726561746572207468616e2030a26474726f6e582011b383d3f69d8d4f91a10060595d757457f622504347b3d4923a650570ad7b8964736f6c63430005090031a26474726f6e58204ed53f9170f34bd5dba28fc3f4f4e64b21fcab4331aaa37e1186bca9fbaf541664736f6c63430005090031";
    byte[] justswapFactoryContractAddress = PublicMethed
        .deployContract(contractName, abi, code, "",
            1000000000L, 0L, 100, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    logger.info("justswapFactoryContractAddress " + WalletClient.encode58Check(justswapFactoryContractAddress));

    // deployJustswapExchange
    contractName = "JustswapExchange";
    abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"tokens_sold\",\"type\":\"uint256\"},{\"name\":\"min_trx\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"},{\"name\":\"recipient\",\"type\":\"address\"}],\"name\":\"tokenToTrxTransferInput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"spender\",\"type\":\"address\"},{\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"trx_bought\",\"type\":\"uint256\"},{\"name\":\"max_tokens\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"},{\"name\":\"recipient\",\"type\":\"address\"}],\"name\":\"tokenToTrxTransferOutput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"from\",\"type\":\"address\"},{\"name\":\"to\",\"type\":\"address\"},{\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"spender\",\"type\":\"address\"},{\"name\":\"addedValue\",\"type\":\"uint256\"}],\"name\":\"increaseAllowance\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"min_liquidity\",\"type\":\"uint256\"},{\"name\":\"max_tokens\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"}],\"name\":\"addLiquidity\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"min_tokens\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"}],\"name\":\"trxToTokenSwapInput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"token_addr\",\"type\":\"address\"}],\"name\":\"setup\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"owner\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokens_bought\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"},{\"name\":\"recipient\",\"type\":\"address\"}],\"name\":\"trxToTokenTransferOutput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"input_amount\",\"type\":\"uint256\"},{\"name\":\"input_reserve\",\"type\":\"uint256\"},{\"name\":\"output_reserve\",\"type\":\"uint256\"}],\"name\":\"getInputPrice\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"tokens_sold\",\"type\":\"uint256\"}],\"name\":\"getTokenToTrxInputPrice\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"factoryAddress\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"trx_sold\",\"type\":\"uint256\"}],\"name\":\"getTrxToTokenInputPrice\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokens_bought\",\"type\":\"uint256\"},{\"name\":\"max_tokens_sold\",\"type\":\"uint256\"},{\"name\":\"max_trx_sold\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"},{\"name\":\"recipient\",\"type\":\"address\"},{\"name\":\"exchange_addr\",\"type\":\"address\"}],\"name\":\"tokenToExchangeTransferOutput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokens_sold\",\"type\":\"uint256\"},{\"name\":\"min_trx\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"}],\"name\":\"tokenToTrxSwapInput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"trx_bought\",\"type\":\"uint256\"},{\"name\":\"max_tokens\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"}],\"name\":\"tokenToTrxSwapOutput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"tokenAddress\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"spender\",\"type\":\"address\"},{\"name\":\"subtractedValue\",\"type\":\"uint256\"}],\"name\":\"decreaseAllowance\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"tokens_bought\",\"type\":\"uint256\"}],\"name\":\"getTrxToTokenOutputPrice\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokens_bought\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"}],\"name\":\"trxToTokenSwapOutput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"to\",\"type\":\"address\"},{\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokens_bought\",\"type\":\"uint256\"},{\"name\":\"max_tokens_sold\",\"type\":\"uint256\"},{\"name\":\"max_trx_sold\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"},{\"name\":\"token_addr\",\"type\":\"address\"}],\"name\":\"tokenToTokenSwapOutput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokens_sold\",\"type\":\"uint256\"},{\"name\":\"min_tokens_bought\",\"type\":\"uint256\"},{\"name\":\"min_trx_bought\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"},{\"name\":\"exchange_addr\",\"type\":\"address\"}],\"name\":\"tokenToExchangeSwapInput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"min_tokens\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"},{\"name\":\"recipient\",\"type\":\"address\"}],\"name\":\"trxToTokenTransferInput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"owner\",\"type\":\"address\"},{\"name\":\"spender\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokens_sold\",\"type\":\"uint256\"},{\"name\":\"min_tokens_bought\",\"type\":\"uint256\"},{\"name\":\"min_trx_bought\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"},{\"name\":\"token_addr\",\"type\":\"address\"}],\"name\":\"tokenToTokenSwapInput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokens_bought\",\"type\":\"uint256\"},{\"name\":\"max_tokens_sold\",\"type\":\"uint256\"},{\"name\":\"max_trx_sold\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"},{\"name\":\"exchange_addr\",\"type\":\"address\"}],\"name\":\"tokenToExchangeSwapOutput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokens_sold\",\"type\":\"uint256\"},{\"name\":\"min_tokens_bought\",\"type\":\"uint256\"},{\"name\":\"min_trx_bought\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"},{\"name\":\"recipient\",\"type\":\"address\"},{\"name\":\"exchange_addr\",\"type\":\"address\"}],\"name\":\"tokenToExchangeTransferInput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"trx_bought\",\"type\":\"uint256\"}],\"name\":\"getTokenToTrxOutputPrice\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokens_bought\",\"type\":\"uint256\"},{\"name\":\"max_tokens_sold\",\"type\":\"uint256\"},{\"name\":\"max_trx_sold\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"},{\"name\":\"recipient\",\"type\":\"address\"},{\"name\":\"token_addr\",\"type\":\"address\"}],\"name\":\"tokenToTokenTransferOutput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokens_sold\",\"type\":\"uint256\"},{\"name\":\"min_tokens_bought\",\"type\":\"uint256\"},{\"name\":\"min_trx_bought\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"},{\"name\":\"recipient\",\"type\":\"address\"},{\"name\":\"token_addr\",\"type\":\"address\"}],\"name\":\"tokenToTokenTransferInput\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"amount\",\"type\":\"uint256\"},{\"name\":\"min_trx\",\"type\":\"uint256\"},{\"name\":\"min_tokens\",\"type\":\"uint256\"},{\"name\":\"deadline\",\"type\":\"uint256\"}],\"name\":\"removeLiquidity\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"output_amount\",\"type\":\"uint256\"},{\"name\":\"input_reserve\",\"type\":\"uint256\"},{\"name\":\"output_reserve\",\"type\":\"uint256\"}],\"name\":\"getOutputPrice\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"fallback\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"buyer\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"trx_sold\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"tokens_bought\",\"type\":\"uint256\"}],\"name\":\"TokenPurchase\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"buyer\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"tokens_sold\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"trx_bought\",\"type\":\"uint256\"}],\"name\":\"TrxPurchase\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"provider\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"trx_amount\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"token_amount\",\"type\":\"uint256\"}],\"name\":\"AddLiquidity\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"provider\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"trx_amount\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"token_amount\",\"type\":\"uint256\"}],\"name\":\"RemoveLiquidity\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"operator\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"trx_balance\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"token_balance\",\"type\":\"uint256\"}],\"name\":\"Snapshot\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"}]";
    code = "60806040526003805460ff19166001179055613ea6806100206000396000f3fe6080604052600436106102255760003560e01c8063999bb7ac11610123578063ce22f858116100ab578063f396b84c1161006f578063f396b84c14610c35578063f3c0efe914610c79578063f552d91b14610ce6578063f88bf15a14610d53578063fd11c22314610dc257610225565b8063ce22f85814610a9e578063dd62ed3e14610ad0578063ddf7e1a714610b25578063ea650c7d14610b63578063ec384a3e14610bc857610225565b8063a7c6d9d9116100f2578063a7c6d9d91461091a578063a8f85ca61461095e578063a9059cbb14610981578063b040d545146109d4578063b1cb43bf14610a3957610225565b8063999bb7ac146107f857806399a13409146108485780639d76ea5814610898578063a457c2d7146108c757610225565b80634bf3e2d0116101b15780639378b0e8116101755780639378b0e81461068957806395d89b41146106cd578063966dae0e146106fc57806397430b4e14610747578063981a13271461078b57610225565b80634bf3e2d01461054857806366d382031461056b57806370a08231146105ba57806387f1ab151461060757806389f2a8711461063957610225565b806318160ddd116101f857806318160ddd1461041157806323b872dd14610440578063313ce5671461049d57806339509351146104cc578063422f10431461051f57610225565b80630127f1421461023657806306fdde03146102a7578063095ea7b31461034b57806314178dda146103b2575b610233346001423333610e12565b50005b34801561024257600080fd5b50d3801561024f57600080fd5b50d2801561025c57600080fd5b506102956004803603608081101561027357600080fd5b50803590602081013590604081013590606001356001600160a01b031661110f565b60408051918252519081900360200190f35b3480156102b357600080fd5b50d380156102c057600080fd5b50d280156102cd57600080fd5b506102d6611151565b6040805160208082528351818301528351919283929083019185019080838360005b838110156103105781810151838201526020016102f8565b50505050905090810190601f16801561033d5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561035757600080fd5b50d3801561036457600080fd5b50d2801561037157600080fd5b5061039e6004803603604081101561038857600080fd5b506001600160a01b0381351690602001356111df565b604080519115158252519081900360200190f35b3480156103be57600080fd5b50d380156103cb57600080fd5b50d280156103d857600080fd5b50610295600480360360808110156103ef57600080fd5b50803590602081013590604081013590606001356001600160a01b03166111f6565b34801561041d57600080fd5b50d3801561042a57600080fd5b50d2801561043757600080fd5b5061029561122f565b34801561044c57600080fd5b50d3801561045957600080fd5b50d2801561046657600080fd5b5061039e6004803603606081101561047d57600080fd5b506001600160a01b03813581169160208101359091169060400135611236565b3480156104a957600080fd5b50d380156104b657600080fd5b50d280156104c357600080fd5b5061029561128e565b3480156104d857600080fd5b50d380156104e557600080fd5b50d280156104f257600080fd5b5061039e6004803603604081101561050957600080fd5b506001600160a01b038135169060200135611294565b6102956004803603606081101561053557600080fd5b50803590602081013590604001356112d0565b6102956004803603604081101561055e57600080fd5b508035906020013561194d565b34801561057757600080fd5b50d3801561058457600080fd5b50d2801561059157600080fd5b506105b8600480360360208110156105a857600080fd5b50356001600160a01b031661195c565b005b3480156105c657600080fd5b50d380156105d357600080fd5b50d280156105e057600080fd5b50610295600480360360208110156105f757600080fd5b50356001600160a01b0316611a6b565b6102956004803603606081101561061d57600080fd5b50803590602081013590604001356001600160a01b0316611a86565b34801561064557600080fd5b50d3801561065257600080fd5b50d2801561065f57600080fd5b506102956004803603606081101561067657600080fd5b5080359060208101359060400135611ac7565b34801561069557600080fd5b50d380156106a257600080fd5b50d280156106af57600080fd5b50610295600480360360208110156106c657600080fd5b5035611b78565b3480156106d957600080fd5b50d380156106e657600080fd5b50d280156106f357600080fd5b506102d6611c55565b34801561070857600080fd5b50d3801561071557600080fd5b50d2801561072257600080fd5b5061072b611cb0565b604080516001600160a01b039092168252519081900360200190f35b34801561075357600080fd5b50d3801561076057600080fd5b50d2801561076d57600080fd5b506102956004803603602081101561078457600080fd5b5035611cbf565b34801561079757600080fd5b50d380156107a457600080fd5b50d280156107b157600080fd5b50610295600480360360c08110156107c857600080fd5b508035906020810135906040810135906060810135906001600160a01b03608082013581169160a0013516611d9a565b34801561080457600080fd5b50d3801561081157600080fd5b50d2801561081e57600080fd5b506102956004803603606081101561083557600080fd5b5080359060208101359060400135611dfd565b34801561085457600080fd5b50d3801561086157600080fd5b50d2801561086e57600080fd5b506102956004803603606081101561088557600080fd5b5080359060208101359060400135611e0c565b3480156108a457600080fd5b50d380156108b157600080fd5b50d280156108be57600080fd5b5061072b611e1b565b3480156108d357600080fd5b50d380156108e057600080fd5b50d280156108ed57600080fd5b5061039e6004803603604081101561090457600080fd5b506001600160a01b038135169060200135611e2a565b34801561092657600080fd5b50d3801561093357600080fd5b50d2801561094057600080fd5b506102956004803603602081101561095757600080fd5b5035611e66565b6102956004803603604081101561097457600080fd5b5080359060200135611f2d565b34801561098d57600080fd5b50d3801561099a57600080fd5b50d280156109a757600080fd5b5061039e600480360360408110156109be57600080fd5b506001600160a01b038135169060200135611f3c565b3480156109e057600080fd5b50d380156109ed57600080fd5b50d280156109fa57600080fd5b50610295600480360360a0811015610a1157600080fd5b50803590602081013590604081013590606081013590608001356001600160a01b0316611f49565b348015610a4557600080fd5b50d38015610a5257600080fd5b50d28015610a5f57600080fd5b50610295600480360360a0811015610a7657600080fd5b50803590602081013590604081013590606081013590608001356001600160a01b0316611fd5565b61029560048036036060811015610ab457600080fd5b50803590602081013590604001356001600160a01b0316611ff0565b348015610adc57600080fd5b50d38015610ae957600080fd5b50d28015610af657600080fd5b5061029560048036036040811015610b0d57600080fd5b506001600160a01b0381358116916020013516612029565b610295600480360360a0811015610b3b57600080fd5b50803590602081013590604081013590606081013590608001356001600160a01b0316612054565b348015610b6f57600080fd5b50d38015610b7c57600080fd5b50d28015610b8957600080fd5b50610295600480360360a0811015610ba057600080fd5b50803590602081013590604081013590606081013590608001356001600160a01b03166120e0565b348015610bd457600080fd5b50d38015610be157600080fd5b50d28015610bee57600080fd5b50610295600480360360c0811015610c0557600080fd5b508035906020810135906040810135906060810135906001600160a01b03608082013581169160a00135166120f1565b348015610c4157600080fd5b50d38015610c4e57600080fd5b50d28015610c5b57600080fd5b5061029560048036036020811015610c7257600080fd5b5035612154565b348015610c8557600080fd5b50d38015610c9257600080fd5b50d28015610c9f57600080fd5b50610295600480360360c0811015610cb657600080fd5b508035906020810135906040810135906060810135906001600160a01b03608082013581169160a001351661222f565b348015610cf257600080fd5b50d38015610cff57600080fd5b50d28015610d0c57600080fd5b50610295600480360360c0811015610d2357600080fd5b508035906020810135906040810135906060810135906001600160a01b03608082013581169160a00135166122c7565b348015610d5f57600080fd5b50d38015610d6c57600080fd5b50d28015610d7957600080fd5b50610da960048036036080811015610d9057600080fd5b5080359060208101359060408101359060600135612353565b6040805192835260208301919091528051918290030190f35b348015610dce57600080fd5b50d38015610ddb57600080fd5b50d28015610de857600080fd5b5061029560048036036060811015610dff57600080fd5b5080359060208101359060400135612742565b60035460009060ff16610e5a576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff19169055428410801590610e745750600086115b8015610e805750600085115b610ed1576040805162461bcd60e51b815260206004820152601760248201527f747278546f546f6b656e496e7075742d72657175697265000000000000000000604482015290519081900360640190fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b158015610f1c57600080fd5b505afa158015610f30573d6000803e3d6000fd5b505050506040513d6020811015610f4657600080fd5b505190506000610f6788610f6130318263ffffffff6127b116565b84611ac7565b905086811015610fbe576040805162461bcd60e51b815260206004820152601860248201527f746f6b656e735f626f756768743c6d696e5f746f6b656e730000000000000000604482015290519081900360640190fd5b600754610fdb906001600160a01b0316858363ffffffff6127f316565b611022576040805162461bcd60e51b81526020600482015260136024820152721cd85999551c985b9cd9995c8819985a5b1959606a1b604482015290519081900360640190fd5b8088866001600160a01b03167fcd60aa75dea3072fbc07ae6d7d856b5dc5f4eee88854f5b4abf7b680ef8bc50f60405160405180910390a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b1580156110a557600080fd5b505afa1580156110b9573d6000803e3d6000fd5b505050506040513d60208110156110cf57600080fd5b50516040513031906001600160a01b03881690600080516020613d8883398151915290600090a49150506003805460ff1916600117905595945050505050565b60006001600160a01b038216301480159061113257506001600160a01b03821615155b61113b57600080fd5b611148858585338661293e565b95945050505050565b6004805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156111d75780601f106111ac576101008083540402835291602001916111d7565b820191906000526020600020905b8154815290600101906020018083116111ba57829003601f168201915b505050505081565b60006111ec338484612b86565b5060015b92915050565b60006001600160a01b038216301480159061121957506001600160a01b03821615155b61122257600080fd5b6111488585853386612c0e565b6002545b90565b6000611243848484612dde565b6001600160a01b03841660009081526001602090815260408083203380855292529091205461128391869161127e908663ffffffff6127b116565b612b86565b5060015b9392505050565b60065481565b3360008181526001602090815260408083206001600160a01b038716845290915281205490916111ec91859061127e908663ffffffff612e9716565b60035460009060ff16611318576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff1916905542821180156113315750600083115b801561133d5750600034115b6113785760405162461bcd60e51b815260040180806020018281038252602b815260200180613da8602b913960400191505060405180910390fd5b600254801561168257600085116113c05760405162461bcd60e51b8152600401808060200182810382526021815260200180613d676021913960400191505060405180910390fd5b60006113d330313463ffffffff6127b116565b600754604080516370a0823160e01b815230600482015290519293506000926001600160a01b03909216916370a0823191602480820192602092909190829003018186803b15801561142457600080fd5b505afa158015611438573d6000803e3d6000fd5b505050506040513d602081101561144e57600080fd5b50519050600061148560016114798561146d348763ffffffff612ef116565b9063ffffffff612f4a16565b9063ffffffff612e9716565b9050600061149d8461146d348863ffffffff612ef116565b90508188101580156114af5750888110155b6114ea5760405162461bcd60e51b815260040180806020018281038252603e815260200180613dd3603e913960400191505060405180910390fd5b3360009081526020819052604090205461150a908263ffffffff612e9716565b3360009081526020819052604090205561152a858263ffffffff612e9716565b60025560075461154b906001600160a01b031633308563ffffffff612f8c16565b61158e576040805162461bcd60e51b815260206004820152600f60248201526e1d1c985b9cd9995c8819985a5b1959608a1b604482015290519081900360640190fd5b6040518290349033907f06239653922ac7bea6aa2b19dc486b9361821d37712eb796adfd38d81de278ca90600090a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b15801561160857600080fd5b505afa15801561161c573d6000803e3d6000fd5b505050506040513d602081101561163257600080fd5b50516040513031903390600080516020613d8883398151915290600090a46040805182815290513391600091600080516020613e328339815191529181900360200190a394506119399350505050565b6008546001600160a01b0316158015906116a657506007546001600160a01b031615155b80156116b55750629896803410155b6116f6576040805162461bcd60e51b815260206004820152600d60248201526c494e56414c49445f56414c554560981b604482015290519081900360640190fd5b600854600754604080516303795fb160e11b81526001600160a01b03928316600482015290513093909216916306f2bf6291602480820192602092909190829003018186803b15801561174857600080fd5b505afa15801561175c573d6000803e3d6000fd5b505050506040513d602081101561177257600080fd5b50516001600160a01b0316146117cf576040805162461bcd60e51b815260206004820152601f60248201527f746f6b656e2061646472657373206e6f74206d6565742065786368616e676500604482015290519081900360640190fd5b30803160028190553360008181526020819052604090208290556007548793611805926001600160a01b03909216919085612f8c565b611847576040805162461bcd60e51b815260206004820152600e60248201526d1d1c985b99995c8819985a5b195960921b604482015290519081900360640190fd5b6040518290349033907f06239653922ac7bea6aa2b19dc486b9361821d37712eb796adfd38d81de278ca90600090a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b1580156118c157600080fd5b505afa1580156118d5573d6000803e3d6000fd5b505050506040513d60208110156118eb57600080fd5b50516040513031903390600080516020613d8883398151915290600090a46040805182815290513391600091600080516020613e328339815191529181900360200190a39250611939915050565b6003805460ff191660011790559392505050565b60006112873484843333610e12565b6008546001600160a01b031615801561197e57506007546001600160a01b0316155b801561199257506001600160a01b03811615155b6119d5576040805162461bcd60e51b815260206004820152600f60248201526e494e56414c49445f4144445245535360881b604482015290519081900360640190fd5b60088054336001600160a01b031991821617909155600780549091166001600160a01b03831617905560408051808201909152600b8082526a4a7573747377617020563160a81b6020909201918252611a3091600491613c6b565b5060408051808201909152600b8082526a4a555354535741502d563160a81b6020909201918252611a6391600591613c6b565b505060068055565b6001600160a01b031660009081526020819052604090205490565b60006001600160a01b0382163014801590611aa957506001600160a01b03821615155b611ab257600080fd5b611abf84348533866130b2565b949350505050565b60008083118015611ad85750600082115b611b19576040805162461bcd60e51b815260206004820152600d60248201526c494e56414c49445f56414c554560981b604482015290519081900360640190fd5b6000611b2d856103e563ffffffff612ef116565b90506000611b41828563ffffffff612ef116565b90506000611b5b83611479886103e863ffffffff612ef116565b9050611b6d828263ffffffff612f4a16565b979650505050505050565b6000808211611bce576040805162461bcd60e51b815260206004820152601f60248201527f746f6b656e7320736f6c64206d7573742067726561746572207468616e203000604482015290519081900360640190fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b158015611c1957600080fd5b505afa158015611c2d573d6000803e3d6000fd5b505050506040513d6020811015611c4357600080fd5b505190506000611abf84833031611ac7565b6005805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156111d75780601f106111ac576101008083540402835291602001916111d7565b6008546001600160a01b031690565b6000808211611d15576040805162461bcd60e51b815260206004820152601c60248201527f74727820736f6c64206d7573742067726561746572207468616e203000000000604482015290519081900360640190fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b158015611d6057600080fd5b505afa158015611d74573d6000803e3d6000fd5b505050506040513d6020811015611d8a57600080fd5b5051905061128783303183611ac7565b60006001600160a01b038316301415611dee576040805162461bcd60e51b81526020600482015260116024820152701a5b1b1959d85b081c9958da5c1a595b9d607a1b604482015290519081900360640190fd5b611b6d87878787338888613327565b6000611abf848484333361293e565b6000611abf8484843333612c0e565b6007546001600160a01b031690565b3360008181526001602090815260408083206001600160a01b038716845290915281205490916111ec91859061127e908663ffffffff6127b116565b6000808211611ea65760405162461bcd60e51b8152600401808060200182810382526021815260200180613e526021913960400191505060405180910390fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b158015611ef157600080fd5b505afa158015611f05573d6000803e3d6000fd5b505050506040513d6020811015611f1b57600080fd5b505190506000611abf84303184612742565b600061128783348433336130b2565b60006111ec338484612dde565b600854604080516303795fb160e11b81526001600160a01b0384811660048301529151600093849316916306f2bf62916024808301926020929190829003018186803b158015611f9857600080fd5b505afa158015611fac573d6000803e3d6000fd5b505050506040513d6020811015611fc257600080fd5b50519050611b6d87878787338087613327565b6000611fe686868686333388613786565b9695505050505050565b60006001600160a01b038216301480159061201357506001600160a01b03821615155b61201c57600080fd5b611abf3485853386610e12565b6001600160a01b03918216600090815260016020908152604080832093909416825291909152205490565b600854604080516303795fb160e11b81526001600160a01b0384811660048301529151600093849316916306f2bf62916024808301926020929190829003018186803b1580156120a357600080fd5b505afa1580156120b7573d6000803e3d6000fd5b505050506040513d60208110156120cd57600080fd5b50519050611b6d87878787338087613786565b6000611fe686868686333388613327565b60006001600160a01b038316301415612145576040805162461bcd60e51b81526020600482015260116024820152701a5b1b1959d85b081c9958da5c1a595b9d607a1b604482015290519081900360640190fd5b611b6d87878787338888613786565b60008082116121aa576040805162461bcd60e51b815260206004820152601e60248201527f74727820626f75676874206d7573742067726561746572207468616e20300000604482015290519081900360640190fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b1580156121f557600080fd5b505afa158015612209573d6000803e3d6000fd5b505050506040513d602081101561221f57600080fd5b5051905061128783823031612742565b600854604080516303795fb160e11b81526001600160a01b0384811660048301529151600093849316916306f2bf62916024808301926020929190829003018186803b15801561227e57600080fd5b505afa158015612292573d6000803e3d6000fd5b505050506040513d60208110156122a857600080fd5b505190506122bb88888888338987613327565b98975050505050505050565b600854604080516303795fb160e11b81526001600160a01b0384811660048301529151600093849316916306f2bf62916024808301926020929190829003018186803b15801561231657600080fd5b505afa15801561232a573d6000803e3d6000fd5b505050506040513d602081101561234057600080fd5b505190506122bb88888888338987613786565b600354600090819060ff1661239d576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff1916905585158015906123b557504283115b80156123c15750600085115b80156123cd5750600084115b612419576040805162461bcd60e51b8152602060048201526018602482015277696c6c6567616c20696e70757420706172616d657465727360401b604482015290519081900360640190fd5b600254806124585760405162461bcd60e51b8152600401808060200182810382526023815260200180613d446023913960400191505060405180910390fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b1580156124a357600080fd5b505afa1580156124b7573d6000803e3d6000fd5b505050506040513d60208110156124cd57600080fd5b505190506000826124e58a303163ffffffff612ef116565b816124ec57fe5b0490506000836125028b8563ffffffff612ef116565b8161250957fe5b04905088821015801561251c5750878110155b61256d576040805162461bcd60e51b815260206004820152601d60248201527f6d696e5f746f6b656e206f72206d696e5f747278206e6f74206d656574000000604482015290519081900360640190fd5b3360009081526020819052604090205461258d908b63ffffffff6127b116565b336000908152602081905260409020556125ad848b63ffffffff6127b116565b600255604051339083156108fc029084906000818181858888f193505050501580156125dd573d6000803e3d6000fd5b506007546125fb906001600160a01b0316338363ffffffff6127f316565b61263e576040805162461bcd60e51b815260206004820152600f60248201526e1d1c985b9cd9995c8819985a5b1959608a1b604482015290519081900360640190fd5b6040518190839033907f0fbf06c058b90cb038a618f8c2acbf6145f8b3570fd1fa56abb8f0f3f05b36e890600090a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b1580156126b857600080fd5b505afa1580156126cc573d6000803e3d6000fd5b505050506040513d60208110156126e257600080fd5b50516040513031903390600080516020613d8883398151915290600090a4604080518b815290516000913391600080516020613e328339815191529181900360200190a36003805460ff1916600117905590999098509650505050505050565b600080831180156127535750600082115b61275c57600080fd5b60006127806103e8612774868863ffffffff612ef116565b9063ffffffff612ef116565b9050600061279a6103e5612774868963ffffffff6127b116565b9050611fe66001611479848463ffffffff612f4a16565b600061128783836040518060400160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f770000815250613b6f565b604080516001600160a01b038481166024830152604480830185905283518084039091018152606490920183526020820180516001600160e01b031663a9059cbb60e01b1781529251825160009485946060948a16939092909182918083835b602083106128725780518252601f199092019160209182019101612853565b6001836020036101000a0380198251168184511680821785525050505050509050019150506000604051808303816000865af19150503d80600081146128d4576040519150601f19603f3d011682016040523d82523d6000602084013e6128d9565b606091505b5090925090506001600160a01b03861673a614f803b6fd780986a42c78ec9c7f77e6ded13c141561290c57509050611287565b818015611fe6575080511580611fe6575080806020019051602081101561293257600080fd5b50519695505050505050565b60035460009060ff16612986576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff191690554284108015906129a05750600086115b80156129ac5750600085115b6129b557600080fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b158015612a0057600080fd5b505afa158015612a14573d6000803e3d6000fd5b505050506040513d6020811015612a2a57600080fd5b505190506000612a3c88833031611ac7565b90508087811015612a4c57600080fd5b6040516001600160a01b0386169082156108fc029083906000818181858888f19350505050158015612a82573d6000803e3d6000fd5b50600754612aa1906001600160a01b031687308c63ffffffff612f8c16565b612aaa57600080fd5b8089876001600160a01b0316600080516020613d0483398151915260405160405180910390a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b158015612b1b57600080fd5b505afa158015612b2f573d6000803e3d6000fd5b505050506040513d6020811015612b4557600080fd5b50516040513031906001600160a01b03891690600080516020613d8883398151915290600090a4925050506003805460ff1916600117905595945050505050565b6001600160a01b038216612b9957600080fd5b6001600160a01b038316612bac57600080fd5b6001600160a01b03808416600081815260016020908152604080832094871680845294825291829020859055815185815291517f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b9259281900390910190a3505050565b60035460009060ff16612c56576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff19169055428410801590612c705750600086115b612c7957600080fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b158015612cc457600080fd5b505afa158015612cd8573d6000803e3d6000fd5b505050506040513d6020811015612cee57600080fd5b505190506000612d0088833031612742565b905080871015612d0f57600080fd5b6040516001600160a01b0385169089156108fc02908a906000818181858888f19350505050158015612d45573d6000803e3d6000fd5b50600754612d64906001600160a01b031686308463ffffffff612f8c16565b612d6d57600080fd5b8781866001600160a01b0316600080516020613d0483398151915260405160405180910390a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b1580156110a557600080fd5b6001600160a01b038216612df157600080fd5b6001600160a01b038316600090815260208190526040902054612e1a908263ffffffff6127b116565b6001600160a01b038085166000908152602081905260408082209390935590841681522054612e4f908263ffffffff612e9716565b6001600160a01b03808416600081815260208181526040918290209490945580518581529051919392871692600080516020613e3283398151915292918290030190a3505050565b600082820183811015611287576040805162461bcd60e51b815260206004820152601b60248201527f536166654d6174683a206164646974696f6e206f766572666c6f770000000000604482015290519081900360640190fd5b600082612f00575060006111f0565b82820282848281612f0d57fe5b04146112875760405162461bcd60e51b8152600401808060200182810382526021815260200180613e116021913960400191505060405180910390fd5b600061128783836040518060400160405280601a81526020017f536166654d6174683a206469766973696f6e206279207a65726f000000000000815250613c06565b604080516001600160a01b0385811660248301528481166044830152606480830185905283518084039091018152608490920183526020820180516001600160e01b03166323b872dd60e01b1781529251825160009485946060948b16939092909182918083835b602083106130135780518252601f199092019160209182019101612ff4565b6001836020036101000a0380198251168184511680821785525050505050509050019150506000604051808303816000865af19150503d8060008114613075576040519150601f19603f3d011682016040523d82523d6000602084013e61307a565b606091505b5091509150818015611b6d575080511580611b6d57508080602001905160208110156130a557600080fd5b5051979650505050505050565b60035460009060ff166130fa576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff191690554284108015906131145750600086115b80156131205750600085115b61312957600080fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b15801561317457600080fd5b505afa158015613188573d6000803e3d6000fd5b505050506040513d602081101561319e57600080fd5b5051905060006131bf886131b930318a63ffffffff6127b116565b84612742565b905060006131d3888363ffffffff6127b116565b90508015613213576040516001600160a01b0387169082156108fc029083906000818181858888f19350505050158015613211573d6000803e3d6000fd5b505b600754613230906001600160a01b0316868b63ffffffff6127f316565b61323957600080fd5b8882876001600160a01b03167fcd60aa75dea3072fbc07ae6d7d856b5dc5f4eee88854f5b4abf7b680ef8bc50f60405160405180910390a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b1580156132bc57600080fd5b505afa1580156132d0573d6000803e3d6000fd5b505050506040513d60208110156132e657600080fd5b50516040513031906001600160a01b03891690600080516020613d8883398151915290600090a4509150506003805460ff1916600117905595945050505050565b60035460009060ff1661336f576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff1916905542851080159061339457506000881180156133945750600086115b6133e0576040805162461bcd60e51b8152602060048201526018602482015277696c6c6567616c20696e70757420706172616d657465727360401b604482015290519081900360640190fd5b6001600160a01b038216301480159061340157506001600160a01b03821615155b61344a576040805162461bcd60e51b815260206004820152601560248201527434b63632b3b0b61032bc31b430b733b29030b2323960591b604482015290519081900360640190fd5b6000826001600160a01b031663a7c6d9d98a6040518263ffffffff1660e01b81526004018082815260200191505060206040518083038186803b15801561349057600080fd5b505afa1580156134a4573d6000803e3d6000fd5b505050506040513d60208110156134ba57600080fd5b5051600754604080516370a0823160e01b815230600482015290519293506000926001600160a01b03909216916370a0823191602480820192602092909190829003018186803b15801561350d57600080fd5b505afa158015613521573d6000803e3d6000fd5b505050506040513d602081101561353757600080fd5b50519050600061354983833031612742565b9050808a1015801561355b5750828910155b6135ac576040805162461bcd60e51b815260206004820152601a60248201527f6d617820746f6b656e20736f6c64206e6f74206d617463686564000000000000604482015290519081900360640190fd5b6007546135ca906001600160a01b031688308463ffffffff612f8c16565b61360d576040805162461bcd60e51b815260206004820152600f60248201526e1d1c985b9cd9995c8819985a5b1959608a1b604482015290519081900360640190fd5b6000856001600160a01b03166387f1ab15858e8c8b6040518563ffffffff1660e01b815260040180848152602001838152602001826001600160a01b03166001600160a01b0316815260200193505050506020604051808303818588803b15801561367757600080fd5b505af115801561368b573d6000803e3d6000fd5b50505050506040513d60208110156136a257600080fd5b5051604051909150849083906001600160a01b038b1690600080516020613d0483398151915290600090a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b15801561371857600080fd5b505afa15801561372c573d6000803e3d6000fd5b505050506040513d602081101561374257600080fd5b50516040513031906001600160a01b038b1690600080516020613d8883398151915290600090a450925050506003805460ff19166001179055979650505050505050565b60035460009060ff166137ce576040805162461bcd60e51b815260206004820152601f6024820152600080516020613d24833981519152604482015290519081900360640190fd5b6003805460ff191690554285108015906137e85750600088115b80156137f45750600087115b80156138005750600086115b61384c576040805162461bcd60e51b8152602060048201526018602482015277696c6c6567616c20696e70757420706172616d657465727360401b604482015290519081900360640190fd5b6001600160a01b038216301480159061386d57506001600160a01b03821615155b6138b6576040805162461bcd60e51b815260206004820152601560248201527434b63632b3b0b61032bc31b430b733b29030b2323960591b604482015290519081900360640190fd5b600754604080516370a0823160e01b815230600482015290516000926001600160a01b0316916370a08231916024808301926020929190829003018186803b15801561390157600080fd5b505afa158015613915573d6000803e3d6000fd5b505050506040513d602081101561392b57600080fd5b50519050600061393d8a833031611ac7565b90508088811015613995576040805162461bcd60e51b815260206004820152601a60248201527f6d696e2074727820626f75676874206e6f74206d617463686564000000000000604482015290519081900360640190fd5b6007546139b3906001600160a01b031688308e63ffffffff612f8c16565b6139f6576040805162461bcd60e51b815260206004820152600f60248201526e1d1c985b9cd9995c8819985a5b1959608a1b604482015290519081900360640190fd5b6000856001600160a01b031663ce22f858838d8c8b6040518563ffffffff1660e01b815260040180848152602001838152602001826001600160a01b03166001600160a01b0316815260200193505050506020604051808303818588803b158015613a6057600080fd5b505af1158015613a74573d6000803e3d6000fd5b50505050506040513d6020811015613a8b57600080fd5b505160405190915082908d906001600160a01b038b1690600080516020613d0483398151915290600090a4600754604080516370a0823160e01b815230600482015290516001600160a01b03909216916370a0823191602480820192602092909190829003018186803b158015613b0157600080fd5b505afa158015613b15573d6000803e3d6000fd5b505050506040513d6020811015613b2b57600080fd5b50516040513031906001600160a01b038b1690600080516020613d8883398151915290600090a493505050506003805460ff19166001179055979650505050505050565b60008184841115613bfe5760405162461bcd60e51b81526004018080602001828103825283818151815260200191508051906020019080838360005b83811015613bc3578181015183820152602001613bab565b50505050905090810190601f168015613bf05780820380516001836020036101000a031916815260200191505b509250505060405180910390fd5b505050900390565b60008183613c555760405162461bcd60e51b8152602060048201818152835160248401528351909283926044909101919085019080838360008315613bc3578181015183820152602001613bab565b506000838581613c6157fe5b0495945050505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10613cac57805160ff1916838001178555613cd9565b82800160010185558215613cd9579182015b82811115613cd9578251825591602001919060010190613cbe565b50613ce5929150613ce9565b5090565b61123391905b80821115613ce55760008155600101613cef56fedad9ec5c9b9c82bf6927bf0b64293dcdd1f82c92793aef3c5f26d7b93a4a53065265656e7472616e637947756172643a207265656e7472616e742063616c6c00746f74616c5f6c6971756964697479206d7573742067726561746572207468616e20306d696e5f6c6971756964697479206d7573742067726561746572207468616e2030cc7244d3535e7639366f8c5211527112e01de3ec7449ee3a6e66b007f4065a704a75737445786368616e6765236164644c69717569646974793a20494e56414c49445f415247554d454e546d617820746f6b656e73206e6f74206d656574206f72206c69717569646974795f6d696e746564206e6f74206d656574206d696e5f6c6971756964697479536166654d6174683a206d756c7469706c69636174696f6e206f766572666c6f77ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef746f6b656e7320626f75676874206d7573742067726561746572207468616e2030a26474726f6e582011b383d3f69d8d4f91a10060595d757457f622504347b3d4923a650570ad7b8964736f6c63430005090031";
    byte[] justswapExchangeContractAddress = PublicMethed
        .deployContract(contractName, abi, code, "",
            1000000000L, 0L, 100, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    logger.info("justswapExchangeContractAddress " + WalletClient.encode58Check(justswapExchangeContractAddress));

    // initializeFactory
    String data = "\"" + Base58.encode58Check(justswapExchangeContractAddress) + "\"";
    String txid = PublicMethed.triggerContract(justswapFactoryContractAddress, "initializeFactory(address)", data, false,
            0, 1000000000L, PublicMethed.getFinalAddress(triggerOwnerKey), triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    // createExchange1
    contractName = "Jst";
    abi = "[{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"stop\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"burn\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"stopped\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"start\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_name\",\"type\":\"string\"}],\"name\":\"setName\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"_addressFounder\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"}]";
    code = "60c0604052600360808190527f4a7374000000000000000000000000000000000000000000000000000000000060a090815261003e9160009190610168565b506040805180820190915260078082527f4a73747465737400000000000000000000000000000000000000000000000000602090920191825261008391600191610168565b5060126002556000600555600680546001600160a81b03191690553480156100aa57600080fd5b50d380156100b757600080fd5b50d280156100c457600080fd5b50604051610b3c380380610b3c833981810160405260208110156100e757600080fd5b505160068054610100600160a81b031916336101000217905569152d02c7e14af680000060058190556001600160a01b0382166000818152600360209081526040808320859055805194855251929391927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9281900390910190a350610203565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106101a957805160ff19168380011785556101d6565b828001600101855582156101d6579182015b828111156101d65782518255916020019190600101906101bb565b506101e29291506101e6565b5090565b61020091905b808211156101e257600081556001016101ec565b90565b61092a806102126000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50600436106101045760003560e01c806370a08231116100a6578063a9059cbb11610080578063a9059cbb1461027b578063be9a6555146102a7578063c47f0027146102af578063dd62ed3e1461035557610104565b806370a082311461024557806375f12b211461026b57806395d89b411461027357610104565b806318160ddd116100e257806318160ddd146101d057806323b872dd146101ea578063313ce5671461022057806342966c681461022857610104565b806306fdde031461010957806307da68f514610186578063095ea7b314610190575b600080fd5b610111610383565b6040805160208082528351818301528351919283929083019185019080838360005b8381101561014b578181015183820152602001610133565b50505050905090810190601f1680156101785780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b61018e610411565b005b6101bc600480360360408110156101a657600080fd5b506001600160a01b038135169060200135610439565b604080519115158252519081900360200190f35b6101d86104ee565b60408051918252519081900360200190f35b6101bc6004803603606081101561020057600080fd5b506001600160a01b038135811691602081013590911690604001356104f4565b6101d861060f565b61018e6004803603602081101561023e57600080fd5b5035610615565b6101d86004803603602081101561025b57600080fd5b50356001600160a01b03166106ac565b6101bc6106be565b6101116106c7565b6101bc6004803603604081101561029157600080fd5b506001600160a01b038135169060200135610721565b61018e6107e9565b61018e600480360360208110156102c557600080fd5b8101906020810181356401000000008111156102e057600080fd5b8201836020820111156102f257600080fd5b8035906020019184600183028401116401000000008311171561031457600080fd5b91908080601f01602080910402602001604051908101604052809392919081815260200183838082843760009201919091525092955061080e945050505050565b6101d86004803603604081101561036b57600080fd5b506001600160a01b038135811691602001351661083e565b6000805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156104095780601f106103de57610100808354040283529160200191610409565b820191906000526020600020905b8154815290600101906020018083116103ec57829003601f168201915b505050505081565b60065461010090046001600160a01b0316331461042a57fe5b6006805460ff19166001179055565b60065460009060ff161561044957fe5b3361045057fe5b81158061047e57503360009081526004602090815260408083206001600160a01b0387168452909152902054155b61048757600080fd5b3360008181526004602090815260408083206001600160a01b03881680855290835292819020869055805186815290519293927f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925929181900390910190a350600192915050565b60055481565b60065460009060ff161561050457fe5b3361050b57fe5b6001600160a01b03841660009081526003602052604090205482111561053057600080fd5b6001600160a01b038316600090815260036020526040902054828101101561055757600080fd5b6001600160a01b038416600090815260046020908152604080832033845290915290205482111561058757600080fd5b6001600160a01b03808416600081815260036020908152604080832080548801905593881680835284832080548890039055600482528483203384528252918490208054879003905583518681529351929391927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9281900390910190a35060019392505050565b60025481565b3360009081526003602052604090205481111561063157600080fd5b336000818152600360209081526040808320805486900390558280527f3617319a054d772f909f7c479a2cebe5066e836a939412e32403c99029b92eff805486019055805185815290519293927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a350565b60036020526000908152604090205481565b60065460ff1681565b60018054604080516020600284861615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156104095780601f106103de57610100808354040283529160200191610409565b60065460009060ff161561073157fe5b3361073857fe5b3360009081526003602052604090205482111561075457600080fd5b6001600160a01b038316600090815260036020526040902054828101101561077b57600080fd5b336000818152600360209081526040808320805487900390556001600160a01b03871680845292819020805487019055805186815290519293927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a350600192915050565b60065461010090046001600160a01b0316331461080257fe5b6006805460ff19169055565b60065461010090046001600160a01b0316331461082757fe5b805161083a90600090602084019061085b565b5050565b600460209081526000928352604080842090915290825290205481565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061089c57805160ff19168380011785556108c9565b828001600101855582156108c9579182015b828111156108c95782518255916020019190600101906108ae565b506108d59291506108d9565b5090565b6108f391905b808211156108d557600081556001016108df565b9056fea26474726f6e582008be7ae776dbe78db228ddf19117fba42e9393c2f1e93a6666b2e29ee5b38d7664736f6c63430005090031";
    String constructorStr = "constructor(address)";
    data = "\"" + Base58.encode58Check(PublicMethed.getFinalAddress(triggerOwnerKey)) + "\"";
    txid = PublicMethed
        .deployContractWithConstantParame(contractName, abi, code, constructorStr, data, "",
            1000000000L, 0L, 100, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> info = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    jstAddress = info.get().getContractAddress().toByteArray();
    newContractAddress = WalletClient.encode58Check(jstAddress);
    oldAddress = readWantedText("stress.conf", "jstAddress");
    newAddress = "  jstAddress = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);

    data = "\"" + Base58.encode58Check(jstAddress) + "\"";
    txid = PublicMethed
        .triggerContract(justswapFactoryContractAddress, "createExchange(address)", data, false,
            0, 1000000000L, PublicMethed.getFinalAddress(triggerOwnerKey), triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    byte[] a = infoById.get().getContractResult(0).toByteArray();
    byte[] d = subByte(a, 12, 20);
    logger.info("41" + ByteArray.toHexString(d));
    String contractResult = "41" + ByteArray.toHexString(d);
    Assert.assertNotNull(contractResult);
    byte[] JstExchangeAddress = ByteArray.fromHexString(contractResult);
    logger.info("JstExchangeAddress: " + Base58.encode58Check(JstExchangeAddress));

    // createExchange2
    contractName = "Tst";
    abi = "[{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"stop\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"burn\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"stopped\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"start\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_name\",\"type\":\"string\"}],\"name\":\"setName\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"_addressFounder\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"}]";
    code = "60c0604052600360808190527f547374000000000000000000000000000000000000000000000000000000000060a090815261003e9160009190610166565b506040805180820190915260078082527f5473747465737400000000000000000000000000000000000000000000000000602090920191825261008391600191610166565b50600a6002556000600555600680546001600160a81b03191690553480156100aa57600080fd5b50d380156100b757600080fd5b50d280156100c457600080fd5b50604051610b3a380380610b3a833981810160405260208110156100e757600080fd5b505160068054610100600160a81b0319163361010002179055678ac7230489e8000060058190556001600160a01b0382166000818152600360209081526040808320859055805194855251929391927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9281900390910190a350610201565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106101a757805160ff19168380011785556101d4565b828001600101855582156101d4579182015b828111156101d45782518255916020019190600101906101b9565b506101e09291506101e4565b5090565b6101fe91905b808211156101e057600081556001016101ea565b90565b61092a806102106000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50600436106101045760003560e01c806370a08231116100a6578063a9059cbb11610080578063a9059cbb1461027b578063be9a6555146102a7578063c47f0027146102af578063dd62ed3e1461035557610104565b806370a082311461024557806375f12b211461026b57806395d89b411461027357610104565b806318160ddd116100e257806318160ddd146101d057806323b872dd146101ea578063313ce5671461022057806342966c681461022857610104565b806306fdde031461010957806307da68f514610186578063095ea7b314610190575b600080fd5b610111610383565b6040805160208082528351818301528351919283929083019185019080838360005b8381101561014b578181015183820152602001610133565b50505050905090810190601f1680156101785780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b61018e610411565b005b6101bc600480360360408110156101a657600080fd5b506001600160a01b038135169060200135610439565b604080519115158252519081900360200190f35b6101d86104ee565b60408051918252519081900360200190f35b6101bc6004803603606081101561020057600080fd5b506001600160a01b038135811691602081013590911690604001356104f4565b6101d861060f565b61018e6004803603602081101561023e57600080fd5b5035610615565b6101d86004803603602081101561025b57600080fd5b50356001600160a01b03166106ac565b6101bc6106be565b6101116106c7565b6101bc6004803603604081101561029157600080fd5b506001600160a01b038135169060200135610721565b61018e6107e9565b61018e600480360360208110156102c557600080fd5b8101906020810181356401000000008111156102e057600080fd5b8201836020820111156102f257600080fd5b8035906020019184600183028401116401000000008311171561031457600080fd5b91908080601f01602080910402602001604051908101604052809392919081815260200183838082843760009201919091525092955061080e945050505050565b6101d86004803603604081101561036b57600080fd5b506001600160a01b038135811691602001351661083e565b6000805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156104095780601f106103de57610100808354040283529160200191610409565b820191906000526020600020905b8154815290600101906020018083116103ec57829003601f168201915b505050505081565b60065461010090046001600160a01b0316331461042a57fe5b6006805460ff19166001179055565b60065460009060ff161561044957fe5b3361045057fe5b81158061047e57503360009081526004602090815260408083206001600160a01b0387168452909152902054155b61048757600080fd5b3360008181526004602090815260408083206001600160a01b03881680855290835292819020869055805186815290519293927f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925929181900390910190a350600192915050565b60055481565b60065460009060ff161561050457fe5b3361050b57fe5b6001600160a01b03841660009081526003602052604090205482111561053057600080fd5b6001600160a01b038316600090815260036020526040902054828101101561055757600080fd5b6001600160a01b038416600090815260046020908152604080832033845290915290205482111561058757600080fd5b6001600160a01b03808416600081815260036020908152604080832080548801905593881680835284832080548890039055600482528483203384528252918490208054879003905583518681529351929391927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9281900390910190a35060019392505050565b60025481565b3360009081526003602052604090205481111561063157600080fd5b336000818152600360209081526040808320805486900390558280527f3617319a054d772f909f7c479a2cebe5066e836a939412e32403c99029b92eff805486019055805185815290519293927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a350565b60036020526000908152604090205481565b60065460ff1681565b60018054604080516020600284861615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156104095780601f106103de57610100808354040283529160200191610409565b60065460009060ff161561073157fe5b3361073857fe5b3360009081526003602052604090205482111561075457600080fd5b6001600160a01b038316600090815260036020526040902054828101101561077b57600080fd5b336000818152600360209081526040808320805487900390556001600160a01b03871680845292819020805487019055805186815290519293927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a350600192915050565b60065461010090046001600160a01b0316331461080257fe5b6006805460ff19169055565b60065461010090046001600160a01b0316331461082757fe5b805161083a90600090602084019061085b565b5050565b600460209081526000928352604080842090915290825290205481565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061089c57805160ff19168380011785556108c9565b828001600101855582156108c9579182015b828111156108c95782518255916020019190600101906108ae565b506108d59291506108d9565b5090565b6108f391905b808211156108d557600081556001016108df565b9056fea26474726f6e5820bd5059f735fa6a144dc562e30614a8d104f7abcbe623bd1b7625586622f3a97964736f6c63430005090031";
    data = "\"" + Base58.encode58Check(PublicMethed.getFinalAddress(triggerOwnerKey)) + "\"";
    txid = PublicMethed
        .deployContractWithConstantParame(contractName, abi, code, constructorStr, data, "",
            1000000000L, 0L, 100, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    info = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    byte[] TstAddress = info.get().getContractAddress().toByteArray();
    logger.info("TstAddress " + WalletClient.encode58Check(TstAddress));

    data = "\"" + Base58.encode58Check(TstAddress) + "\"";
    txid = PublicMethed
        .triggerContract(justswapFactoryContractAddress, "createExchange(address)", data, false,
            0, 1000000000L, PublicMethed.getFinalAddress(triggerOwnerKey), triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    a = infoById.get().getContractResult(0).toByteArray();
    d = subByte(a, 12, 20);
    contractResult = "41" + ByteArray.toHexString(d);
    tstExchangeAddress = ByteArray.fromHexString(contractResult);
    newContractAddress = WalletClient.encode58Check(tstExchangeAddress);
    oldAddress = readWantedText("stress.conf", "tstExchangeAddress");
    newAddress = "  tstExchangeAddress = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);

    // addLiquidity1
    data = "\"" + Base58.encode58Check(JstExchangeAddress) + "\",\"-1\"";
    txid = PublicMethed
        .triggerContract(jstAddress, "approve(address,uint256)", data, false,
            0, 1000000000L, PublicMethed.getFinalAddress(triggerOwnerKey), triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    java.util.Date date = null;
    try {
      date = df.parse("2025-12-07");
    } catch (ParseException e) {
      e.printStackTrace();
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    long timestamp = cal.getTimeInMillis();
    long deadline = timestamp / 1000;
    String hex = Integer.toHexString(Integer.parseInt(String.valueOf(deadline)));
    String s = addZeroForNum(hex, 64);
    String data1 = "0000000000000000000000000000000000000000000000000000000000000001"
        + "00000000000000000000000000000000000000000000152d02c7e14af6800000"
        + s;
    txid = PublicMethed
        .triggerContract(JstExchangeAddress, "addLiquidity(uint256,uint256,uint256)", data1, true,
            100000000000l, 1000000000L, PublicMethed.getFinalAddress(triggerOwnerKey), triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    // addLiquidity2
    data = "\"" + Base58.encode58Check(tstExchangeAddress) + "\",\"-1\"";
    txid = PublicMethed
        .triggerContract(TstAddress, "approve(address,uint256)", data, false,
            0, 1000000000L, PublicMethed.getFinalAddress(triggerOwnerKey), triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);

    data1 = "0000000000000000000000000000000000000000000000000000000000000001"
        + "00000000000000000000000000000000000000000000000000038d7ea4c68000"
        + s;
    txid = PublicMethed
        .triggerContract(tstExchangeAddress, "addLiquidity(uint256,uint256,uint256)", data1, true,
            100000000000l, 1000000000L, PublicMethed.getFinalAddress(triggerOwnerKey), triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  /**
   * constructor.
   */

  @AfterClass
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  public void sendCoinToStressAccount(String key) {
    if (PublicMethed.queryAccount(key, blockingStubFull).getBalance() <= 498879998803847L) {
      PublicMethed.sendcoin(PublicMethed.getFinalAddress(key), 998879998803847L, witness004Address,
          witnessKey004, blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
    }
  }


  public void replacAddressInConfig(String path, String oldAddress, String newAddress) {
    try {
      File file = new File(path);
      FileReader in = new FileReader(file);
      BufferedReader bufIn = new BufferedReader(in);
      CharArrayWriter tempStream = new CharArrayWriter();
      String line = null;
      while ((line = bufIn.readLine()) != null) {
        line = line.replaceAll(oldAddress, newAddress);
        tempStream.write(line);
        tempStream.append(System.getProperty("line.separator"));
      }
      bufIn.close();
      FileWriter out = new FileWriter(file);
      tempStream.writeTo(out);
      out.close();
      System.out.println("====path:" + path);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String readWantedText(String url, String wanted) {
    try {
      FileReader fr = new FileReader(url);
      BufferedReader br = new BufferedReader(fr);
      String temp = "";
      while (temp != null) {
        temp = br.readLine();
        if (temp != null && temp.contains(wanted)) {
          System.out.println(temp);
          return temp;
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  public static void waitProposalApprove(Integer proposalIndex,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Long currentTime = System.currentTimeMillis();
    while (System.currentTimeMillis() <= currentTime + 610000) {
      ChainParameters chainParameters = blockingStubFull
          .getChainParameters(EmptyMessage.newBuilder().build());
      Optional<ChainParameters> getChainParameters = Optional.ofNullable(chainParameters);
      if (getChainParameters.get().getChainParameter(proposalIndex).getValue() == 1L) {
        logger.info("Proposal has been approval");
        return;
      }
      PublicMethed.waitProduceNextBlock(blockingStubFull);
    }


  }


  public static void waitZenTokenFeeProposalApprove(Integer proposalIndex,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Long currentTime = System.currentTimeMillis();
    while (System.currentTimeMillis() <= currentTime + 610000) {
      ChainParameters chainParameters = blockingStubFull
          .getChainParameters(EmptyMessage.newBuilder().build());
      Optional<ChainParameters> getChainParameters = Optional.ofNullable(chainParameters);
      if (getChainParameters.get().getChainParameter(proposalIndex).getValue() == 3L) {
        logger.info("Zen token fee proposal has been approval");
        return;
      }
      PublicMethed.waitProduceNextBlock(blockingStubFull);
    }


  }

  public static void writeTrc20ContractToFile(String contractAddress)
  {
    try{
      File file =new File(Configuration.getByPath("stress.conf").getString("param.trc20ContractAddressFile"));

      if(!file.exists()){
        file.createNewFile();
      }
      FileWriter fileWritter = new FileWriter(file.getName(),true);
      fileWritter.write(contractAddress + "\n");
      fileWritter.close();
      //System.out.println("finish");
    }catch(IOException e){
      e.printStackTrace();
    }
  }

  public byte[] subByte(byte[] b, int off, int length) {
    byte[] b1 = new byte[length];
    System.arraycopy(b, off, b1, 0, length);
    return b1;

  }

  public static String addZeroForNum(String str, int strLength) {
    int strLen = str.length();
    if (strLen < strLength) {
      while (strLen < strLength) {
        StringBuffer sb = new StringBuffer();
        sb.append("0").append(str);// 左补0
        // sb.append(str).append("0");//右补0
        str = sb.toString();
        strLen = str.length();
      }
    }

    return str;
  }
}


