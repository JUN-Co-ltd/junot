package jp.co.jun.edi.api.v1;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.model.FukukitaruDestinationModel;
import jp.co.jun.edi.model.FukukitaruOrderModel;
import jp.co.jun.edi.model.FukukitaruOrderSkuModel;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.FukukitaruMasterConfirmStatusType;
import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import test.utils.LoginUserUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class FukukitaruOrderV1ApiCreateTest {
    // モックサーバー
    @Autowired
    private MockMvc mockServer;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    //@WithMockUser(username = "581209\n000001", password = "H3RYB9F8", roles = { "USER", "JUN" })
    public void test_get_0001() throws Exception {
        final  FukukitaruDestinationModel address = new FukukitaruDestinationModel();
        address.setId(BigInteger.valueOf(1));

        final MUserEntity user = createUser();
        final FukukitaruOrderModel test = new FukukitaruOrderModel();
        test.setId(null);// フクキタル発注:フクキタル発注ID
        test.setFItemId(null);// フクキタル発注:フクキタル品番ID ※システム設定→DB登録時自採番
        test.setBillingCompanyId(BigInteger.valueOf(1));// フクキタル発注:請求先ID
        test.setContractNumber("bbb");// フクキタル発注:契約No
        test.setDeliveryCompanyId(BigInteger.valueOf(1));// フクキタル発注:納入先ID
        test.setDeliveryStaff("ccc");// フクキタル発注:納入先担当者
        test.setMdfMakerFactoryCode("aaa");// フクキタル発注:工場No
        test.setOrderAt(stringToDate("2019/01/01"));// フクキタル発注:発注日
        test.setOrderCode(null);// フクキタル発注:オーダー識別コード
        test.setOrderUserId(null);// フクキタル発注:発注者ユーザID ※システム設定→登録ユーザから自動設定
        test.setPreferredShippingAt(stringToDate("2019/01/02"));// フクキタル発注:希望出荷日
        test.setRepeatNumber(11);// フクキタル発注:リピート数
        test.setSpecialReport("ddd");// フクキタル発注:特記事項
        test.setUrgent(BooleanType.TRUE);// フクキタル発注:緊急
        test.setDeliveryType(FukukitaruMasterDeliveryType.DOMESTIC);// フクキタル発注:手配先
        test.setConfirmStatus(FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED);// フクキタル発注:確定ステータス
        test.setOrderType(FukukitaruMasterOrderType.WASH_NAME);
        test.setOrderSkuWashName(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:洗濯ネーム(1)
        test.setOrderSkuAttentionName(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:アテンションネーム(2)
        test.setOrderSkuWashAuxiliary(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:洗濯同封副資材(3)
        test.setOrderSkuBottomBill(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:下札(4)
        test.setOrderSkuAttentionTag(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:アテンションタグ(5)
        test.setOrderSkuBottomBillAttention(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:アテンション下札(6)
        test.setOrderSkuBottomBillNergyMerit(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:NERGY用メリット下札(7)
        test.setOrderSkuBottomBillAuxiliaryMaterial(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:下札同封副資材(8)
        //*************************************************************/
        // フクキタル品番情報
        //*************************************************************/
        //test.setFItem();// フクキタル品番情報
        //*************************************************************/
        // 発注SKU情報
        //*************************************************************/
        //test.setSkus();// 発注SKU情報のリスト
        //*************************************************************/
        // 組成情報
        //*************************************************************/
        //test.setCompositions();// 組成情報のリスト
        //*************************************************************/
        // 発注情報
        //*************************************************************/
        test.setOrderId(BigInteger.valueOf(1));// 発注情報:発注ID
        //test.setOrderNumber(null);// 発注情報:発注No
        //test.setProductCorrectionDeliveryAt(null);// 発注情報:製品修正納期

        //*************************************************************/
        // 品番情報
        //*************************************************************/
        test.setPartNoId(BigInteger.valueOf(1));// 品番情報:品番ID
        //test.setPartNo(null);// 品番情報:品番
        //test.setProductName(null);// 品番情報:品名
        //test.setMdfMakerName(null);// 品番情報:生産メーカー名

        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/fukukitaru/orders")
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(test)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();
        System.out.println(actual.getResponse().getContentAsString());
    }

    @Test
    //@WithMockUser(username = "581209\n000001", password = "H3RYB9F8", roles = { "USER", "JUN" })
    public void test_get_0002() throws Exception {
        final  FukukitaruDestinationModel address = new FukukitaruDestinationModel();
        address.setId(BigInteger.valueOf(1));

        final MUserEntity user = createUser();
        final FukukitaruOrderModel test = new FukukitaruOrderModel();
        test.setId(null);// フクキタル発注:フクキタル発注ID
        test.setFItemId(null);// フクキタル発注:フクキタル品番ID ※システム設定→DB登録時自採番
        test.setBillingCompanyId(BigInteger.valueOf(1));// フクキタル発注:請求先ID
        test.setContractNumber(null);// フクキタル発注:契約No
        test.setDeliveryCompanyId(BigInteger.valueOf(1));// フクキタル発注:納入先ID
        test.setDeliveryStaff(null);// フクキタル発注:納入先担当者
        test.setMdfMakerFactoryCode(null);// フクキタル発注:工場No
        test.setOrderAt(stringToDate("2019/01/01"));// フクキタル発注:発注日
        test.setOrderCode(null);// フクキタル発注:オーダー識別コード
        test.setOrderUserId(null);// フクキタル発注:発注者ユーザID ※システム設定→登録ユーザから自動設定
        test.setPreferredShippingAt(stringToDate("2019/01/02"));// フクキタル発注:希望出荷日
        test.setRepeatNumber(null);// フクキタル発注:リピート数
        test.setSpecialReport(null);// フクキタル発注:特記事項
        test.setUrgent(BooleanType.TRUE);// フクキタル発注:緊急
        test.setDeliveryType(FukukitaruMasterDeliveryType.DOMESTIC);// フクキタル発注:手配先
        test.setConfirmStatus(FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED);// フクキタル発注:確定ステータス
        test.setOrderType(FukukitaruMasterOrderType.WASH_NAME);

        test.setOrderSkuWashName(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:洗濯ネーム(1)
        test.setOrderSkuAttentionName(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:アテンションネーム(2)
        test.setOrderSkuWashAuxiliary(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:洗濯同封副資材(3)
        test.setOrderSkuBottomBill(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:下札(4)
        test.setOrderSkuAttentionTag(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:アテンションタグ(5)
        test.setOrderSkuBottomBillAttention(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:アテンション下札(6)
        test.setOrderSkuBottomBillNergyMerit(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:NERGY用メリット下札(7)
        test.setOrderSkuBottomBillAuxiliaryMaterial(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;

            {
                add(createFKOrderSkuModel());
            }
        });// フクキタル発注:下札同封副資材(8)
        //*************************************************************/
        // フクキタル品番情報
        //*************************************************************/
        //test.setFItem();// フクキタル品番情報
        //*************************************************************/
        // 発注SKU情報
        //*************************************************************/
        //test.setSkus();// 発注SKU情報のリスト
        //*************************************************************/
        // 組成情報
        //*************************************************************/
        //test.setCompositions();// 組成情報のリスト
        //*************************************************************/
        // 発注情報
        //*************************************************************/
        test.setOrderId(BigInteger.valueOf(1));// 発注情報:発注ID
        //test.setOrderNumber(null);// 発注情報:発注No
        //test.setProductCorrectionDeliveryAt(null);// 発注情報:製品修正納期

        //*************************************************************/
        // 品番情報
        //*************************************************************/
        test.setPartNoId(BigInteger.valueOf(1));// 品番情報:品番ID
        //test.setPartNo(null);// 品番情報:品番
        //test.setProductName(null);// 品番情報:品名
        //test.setMdfMakerName(null);// 品番情報:生産メーカー名

        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/fukukitaru/orders")
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(test)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();
        System.out.println(actual.getResponse().getContentAsString());
    }

    private MUserEntity createUser() {
        MUserEntity user = new MUserEntity();
        user.setId(BigInteger.valueOf(1));
        user.setAccountName("000001");
        user.setCompany("000000");
        user.setPassword("H3RYB9F8");
        user.setAuthority("ROLE_JUN,ROLE_USER");
        return user;
    }

    /**
     * String型の日付をDateに変換.
     *
     * @param strDate 日付
     * @return Date型に変換した日付
     */
    private Date stringToDate(final String strDate) {
        LocalDate ld = LocalDate.parse(strDate, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private FukukitaruOrderSkuModel createFKOrderSkuModel() {
        FukukitaruOrderSkuModel s1 = new FukukitaruOrderSkuModel();
        s1.setColorCode(null);
        s1.setSize(null);
        s1.setOrderLot(1000);
        s1.setMaterialId(BigInteger.valueOf(1));
        return s1;
    }
}
