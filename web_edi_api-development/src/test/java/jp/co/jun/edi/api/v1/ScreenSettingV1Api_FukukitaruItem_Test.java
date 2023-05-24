package jp.co.jun.edi.api.v1;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigInteger;
import java.util.ArrayList;

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
import jp.co.jun.edi.model.ScreenSettingFukukitaruSearchConditionModel;
import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;
import jp.co.jun.edi.type.ScreenSettingFukukitaruMasterType;
import test.utils.LoginUserUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class ScreenSettingV1Api_FukukitaruItem_Test {
    // モックサーバー
    @Autowired
    private MockMvc mockServer;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 期待する結果
     * ・データが取れること
     *
     * 条件
     * ・品番IDがNULL、かつ、
     * ・品種が「BVA」
     *
     * @throws Exception
     */
    @Test
    public void test_get_0001() throws Exception {
        final MUserEntity user = createUserCanFukukitaruRoleJUN();
        final ScreenSettingFukukitaruSearchConditionModel model = new ScreenSettingFukukitaruSearchConditionModel();
        model.setPartNoId(null);
        model.setListMasterType(new ArrayList<ScreenSettingFukukitaruMasterType>() {
            private static final long serialVersionUID = 1L;

            {
                add(ScreenSettingFukukitaruMasterType.TAPE_WIDTH);
                add(ScreenSettingFukukitaruMasterType.TAPE_TYPE);
                add(ScreenSettingFukukitaruMasterType.WASH_NAME_APPENDICES_TERM);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_TAG_APPENDICES_TERM);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_TAG_SEAL);
                add(ScreenSettingFukukitaruMasterType.RECYCL);
                add(ScreenSettingFukukitaruMasterType.CN_DERIVERY_PRODUCT_CATEGORY);
                add(ScreenSettingFukukitaruMasterType.CN_DERIVERY_PRODUCT_TYPE);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_NAME);
                add(ScreenSettingFukukitaruMasterType.WASH_PATTERN);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_TAG);
                add(ScreenSettingFukukitaruMasterType.HANG_TAG);
                add(ScreenSettingFukukitaruMasterType.AUXILIARY_MATERIAL);
                add(ScreenSettingFukukitaruMasterType.BILLING_ADDRESS);
                add(ScreenSettingFukukitaruMasterType.DELIVERY_ADDRESS);
                add(ScreenSettingFukukitaruMasterType.SUPPLIER_ADDRESS);
                add(ScreenSettingFukukitaruMasterType.SKU);
                add(ScreenSettingFukukitaruMasterType.ITEM);
                add(ScreenSettingFukukitaruMasterType.ORDER);
                add(ScreenSettingFukukitaruMasterType.FUKUKITARU_ITEM);
            }
        });
        model.setOrderId(null);
        model.setDeliveryType(null);
        model.setSearchCompanyName(null);
        model.setPartNoKind("BVA");

        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/screenSettings/fukukitaru/items")
                        .with(csrf())
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(model)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();
        System.out.println(actual.getResponse().getContentAsString());
    }

    /**
     * 期待する結果
     * ・データが取れること
     *
     * 条件
     * ・品番IDがNULL、かつ、
     * ・品種がNULL、かつ、
     * ・マスタ種別が「15(納品先)」、かつ、
     * ・検索用会社名が、「ＶＩＳ」
     * @throws Exception
     */
    @Test
    public void test_get_0002() throws Exception {
        final MUserEntity user = createUserCanFukukitaruRoleJUN();
        final ScreenSettingFukukitaruSearchConditionModel model = new ScreenSettingFukukitaruSearchConditionModel();
        model.setDeliveryType(FukukitaruMasterDeliveryType.DOMESTIC);
        model.setListMasterType(new ArrayList<ScreenSettingFukukitaruMasterType>() {
            private static final long serialVersionUID = 1L;

            {
                add(ScreenSettingFukukitaruMasterType.DELIVERY_ADDRESS);
            }
        });
        model.setSearchCompanyName("ＶＩＳ");

        // パラメータモデル： FukukitaruMasterSearchConditionModel
        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/screenSettings/fukukitaru/items")
                        .with(csrf())
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(model)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();
        System.out.println(actual.getResponse().getContentAsString());
    }

    /**
     * 期待する結果
     * ・データが取れること
     *
     * 条件
     * ・品番IDがNULL、かつ、
     * ・品種がNULL、かつ、
     * ・マスタ種別が「15(納品先)」、かつ、
     * ・検索用会社名が、「ＶＩＳ」
     * @throws Exception
     */
    @Test
    public void test_get_0003() throws Exception {
        final MUserEntity user = createUserCanFukukitaruRoleJUN();
        final ScreenSettingFukukitaruSearchConditionModel model = new ScreenSettingFukukitaruSearchConditionModel();
        model.setPartNoId(null);
        model.setListMasterType(new ArrayList<ScreenSettingFukukitaruMasterType>() {
            private static final long serialVersionUID = 1L;

            {
                add(ScreenSettingFukukitaruMasterType.TAPE_WIDTH);
                add(ScreenSettingFukukitaruMasterType.TAPE_TYPE);
                add(ScreenSettingFukukitaruMasterType.WASH_NAME_APPENDICES_TERM);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_TAG_APPENDICES_TERM);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_TAG_SEAL);
                add(ScreenSettingFukukitaruMasterType.RECYCL);
                add(ScreenSettingFukukitaruMasterType.CN_DERIVERY_PRODUCT_CATEGORY);
                add(ScreenSettingFukukitaruMasterType.CN_DERIVERY_PRODUCT_TYPE);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_NAME);
                add(ScreenSettingFukukitaruMasterType.WASH_PATTERN);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_TAG);
                add(ScreenSettingFukukitaruMasterType.HANG_TAG);
                add(ScreenSettingFukukitaruMasterType.AUXILIARY_MATERIAL);
                add(ScreenSettingFukukitaruMasterType.BILLING_ADDRESS);
                add(ScreenSettingFukukitaruMasterType.DELIVERY_ADDRESS);
                add(ScreenSettingFukukitaruMasterType.SUPPLIER_ADDRESS);
                add(ScreenSettingFukukitaruMasterType.SKU);
                add(ScreenSettingFukukitaruMasterType.ITEM);
                add(ScreenSettingFukukitaruMasterType.ORDER);
                add(ScreenSettingFukukitaruMasterType.FUKUKITARU_ITEM);
            }
        });
        model.setOrderId(null);
        model.setDeliveryType(null);
        model.setSearchCompanyName(null);
        model.setPartNoKind(null);

        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/screenSettings/fukukitaru/items")
                        .with(csrf())
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(model)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();
        System.out.println(actual.getResponse().getContentAsString());
    }

    /**
     * 期待する結果
     * ・データが取れないこと
     *
     * 条件
     * ・品番IDがNULL、かつ、
     * ・品種が「BVA」、かつ、
     * ・ログインユーザの会社コードが、フクキタル資材発注が利用できない
     *
     * @throws Exception
     */
    @Test
    public void test_get_0004() throws Exception {
        final MUserEntity user = new MUserEntity();
        user.setId(BigInteger.valueOf(1));
        user.setAccountName("900001");
        user.setCompany("00001 ");
        user.setPassword("H3RYB9F8");
        user.setAuthority("ROLE_USER");

        final ScreenSettingFukukitaruSearchConditionModel model = new ScreenSettingFukukitaruSearchConditionModel();
        model.setPartNoId(null);
        model.setListMasterType(new ArrayList<ScreenSettingFukukitaruMasterType>() {
            private static final long serialVersionUID = 1L;

            {
                add(ScreenSettingFukukitaruMasterType.TAPE_WIDTH);
                add(ScreenSettingFukukitaruMasterType.TAPE_TYPE);
                add(ScreenSettingFukukitaruMasterType.WASH_NAME_APPENDICES_TERM);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_TAG_APPENDICES_TERM);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_TAG_SEAL);
                add(ScreenSettingFukukitaruMasterType.RECYCL);
                add(ScreenSettingFukukitaruMasterType.CN_DERIVERY_PRODUCT_CATEGORY);
                add(ScreenSettingFukukitaruMasterType.CN_DERIVERY_PRODUCT_TYPE);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_NAME);
                add(ScreenSettingFukukitaruMasterType.WASH_PATTERN);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_TAG);
                add(ScreenSettingFukukitaruMasterType.HANG_TAG);
                add(ScreenSettingFukukitaruMasterType.AUXILIARY_MATERIAL);
                add(ScreenSettingFukukitaruMasterType.BILLING_ADDRESS);
                add(ScreenSettingFukukitaruMasterType.DELIVERY_ADDRESS);
                add(ScreenSettingFukukitaruMasterType.SUPPLIER_ADDRESS);
                add(ScreenSettingFukukitaruMasterType.SKU);
                add(ScreenSettingFukukitaruMasterType.ITEM);
                add(ScreenSettingFukukitaruMasterType.ORDER);
                add(ScreenSettingFukukitaruMasterType.FUKUKITARU_ITEM);
            }
        });
        model.setOrderId(null);
        model.setDeliveryType(null);
        model.setSearchCompanyName(null);
        model.setPartNoKind("BVA");

        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/screenSettings/fukukitaru/items")
                        .with(csrf())
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(model)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();
        System.out.println(actual.getResponse().getContentAsString());
    }
    /**
     * 期待する結果
     * ・データが取れないこと
     *
     * 条件
     * ・品番IDがNULL、かつ、
     * ・品種が「AAA」、かつ、
     * ・ログインユーザの会社コードが、フクキタル資材発注が利用できない
     * ※ 会社コード＋ブランドコードの組み合わせでフクキタル資材発注が利用できない
     * @throws Exception
     */
    @Test
    public void test_get_0005() throws Exception {
        final MUserEntity user = createUserCanFukukitaruRoleJUN();

        final ScreenSettingFukukitaruSearchConditionModel model = new ScreenSettingFukukitaruSearchConditionModel();
        model.setPartNoId(null);
        model.setListMasterType(new ArrayList<ScreenSettingFukukitaruMasterType>() {
            private static final long serialVersionUID = 1L;

            {
                add(ScreenSettingFukukitaruMasterType.TAPE_WIDTH);
                add(ScreenSettingFukukitaruMasterType.TAPE_TYPE);
                add(ScreenSettingFukukitaruMasterType.WASH_NAME_APPENDICES_TERM);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_TAG_APPENDICES_TERM);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_TAG_SEAL);
                add(ScreenSettingFukukitaruMasterType.RECYCL);
                add(ScreenSettingFukukitaruMasterType.CN_DERIVERY_PRODUCT_CATEGORY);
                add(ScreenSettingFukukitaruMasterType.CN_DERIVERY_PRODUCT_TYPE);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_NAME);
                add(ScreenSettingFukukitaruMasterType.WASH_PATTERN);
                add(ScreenSettingFukukitaruMasterType.ATTENTION_TAG);
                add(ScreenSettingFukukitaruMasterType.HANG_TAG);
                add(ScreenSettingFukukitaruMasterType.AUXILIARY_MATERIAL);
                add(ScreenSettingFukukitaruMasterType.BILLING_ADDRESS);
                add(ScreenSettingFukukitaruMasterType.DELIVERY_ADDRESS);
                add(ScreenSettingFukukitaruMasterType.SUPPLIER_ADDRESS);
                add(ScreenSettingFukukitaruMasterType.SKU);
                add(ScreenSettingFukukitaruMasterType.ITEM);
                add(ScreenSettingFukukitaruMasterType.ORDER);
                add(ScreenSettingFukukitaruMasterType.FUKUKITARU_ITEM);
            }
        });
        model.setOrderId(null);
        model.setDeliveryType(null);
        model.setSearchCompanyName(null);
        model.setPartNoKind("AAA");

        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/screenSettings/fukukitaru/items")
                        .with(csrf())
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(model)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();
        System.out.println(actual.getResponse().getContentAsString());
    }

//    private MUserEntity createUserCanNotFukukitaru() {
//        final MUserEntity user = new MUserEntity();
//        user.setId(BigInteger.valueOf(1));
//        user.setCompany("000000");
//        user.setAccountName("000001");
//        user.setPassword("H3RYB9F8");
//        user.setAuthority("ROLE_JUN,ROLE_USER");
//        return user;
//    }

    private MUserEntity createUserCanFukukitaruRoleJUN() {
        final MUserEntity user = new MUserEntity();
        user.setId(BigInteger.valueOf(1));
        user.setCompany("581209");
        user.setAccountName("407557");
        user.setPassword("1208");
        user.setAuthority("ROLE_JUN,ROLE_USER");
        return user;
    }
//    private MUserEntity createUserCanFukukitaruRoleMAKER() {
//        final MUserEntity user = new MUserEntity();
//        user.setId(BigInteger.valueOf(1));
//        user.setCompany("25419 ");
//        user.setAccountName("625419");
//        user.setPassword("H3RYB9F8");
//        user.setAuthority("91452");
//        return user;
//    }
}