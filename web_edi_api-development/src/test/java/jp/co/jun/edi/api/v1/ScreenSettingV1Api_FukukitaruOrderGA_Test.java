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

/**
 * 下札資材発注画面想定のAPIテスト.
 * ブランドコード：GA（アダム・エ・ロペ レディース）
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class ScreenSettingV1Api_FukukitaruOrderGA_Test {

    // モックサーバー
    @Autowired
    private MockMvc mockServer;
    @Autowired
    private ObjectMapper objectMapper;


    /**
     * 概要
     *   条件に該当する入力補助セット情報が存在しない場合、空のリスト情報がレスポンスされることを確認する
     *
     * 前提条件
     *   ユーザ権限：JUN権限
     *   フクキタル権限：あり
     *   検索条件
     *     マスタタイプリスト：入力補助セット(27)
     *     品番ID：7
     *     発注ID：1
     *     デリバリ種別：海外(2)
     *     入力補助セットID：なし
     *
     * 期待値
     *
     *
     * @throws Exception
     */
    @Test
    public void test0001_INPUT_ASSIST_SET() throws Exception {
        // ユーザ権限、フクキタル権限
        final MUserEntity user = createUserCanFukukitaruRoleJUN();

        // 検索条件
        final ScreenSettingFukukitaruSearchConditionModel model = new ScreenSettingFukukitaruSearchConditionModel();
        model.setListMasterType(new ArrayList<ScreenSettingFukukitaruMasterType>() {
            private static final long serialVersionUID = 1L;

            {
                add(ScreenSettingFukukitaruMasterType.INPUT_ASSIST_SET);
            }
        });
        model.setPartNoId(BigInteger.valueOf(7));
        model.setOrderId(BigInteger.valueOf(1));
        model.setDeliveryType(FukukitaruMasterDeliveryType.OVERSEES);
        model.setInputAssistId(null);

        // テスト実行
        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/screenSettings/fukukitaru/orders/bottomBill")
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
     * 概要
     *   条件に該当する入力補助セット情報が存在する場合、入力補助セット情報がレスポンスされることを確認する
     *
     * 前提条件
     *   ユーザ権限：JUN権限
     *   フクキタル権限：あり
     *   検索条件
     *     マスタタイプリスト：入力補助セット(27)
     *     品番ID：7
     *     発注ID：1
     *     デリバリ種別：海外(2)
     *     入力補助セットID：なし
     *
     * 期待値
     *
     *
     * @throws Exception
     */
    @Test
    public void test0002_INPUT_ASSIST_SET() throws Exception {
        // ユーザ権限、フクキタル権限
        final MUserEntity user = createUserCanFukukitaruRoleJUN();

        // 検索条件
        final ScreenSettingFukukitaruSearchConditionModel model = new ScreenSettingFukukitaruSearchConditionModel();
        model.setListMasterType(new ArrayList<ScreenSettingFukukitaruMasterType>() {
            private static final long serialVersionUID = 1L;

            {
                add(ScreenSettingFukukitaruMasterType.INPUT_ASSIST_SET);
            }
        });
        model.setPartNoId(BigInteger.valueOf(7));
        model.setOrderId(BigInteger.valueOf(1));
        model.setDeliveryType(FukukitaruMasterDeliveryType.DOMESTIC);
        model.setInputAssistId(null);

        // テスト実行
        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/screenSettings/fukukitaru/orders/bottomBill")
                        .with(csrf())
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(model)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();
        System.out.println(actual.getResponse().getContentAsString());

        // 期待値


    }

    /**
     * 概要
     *   条件に該当する入力補助セット情報が存在する場合、入力補助セット情報がレスポンスされることを確認する
     *
     * 前提条件
     *   ユーザ権限：JUN権限
     *   フクキタル権限：あり
     *   検索条件
     *     マスタタイプリスト：入力補助セット(27)
     *     品番ID：7
     *     発注ID：1
     *     デリバリ種別：海外(2)
     *     入力補助セットID：1
     *
     * 期待値
     *
     *
     * @throws Exception
     */
    @Test
    public void test0003_INPUT_ASSIST_SET() throws Exception {
        // ユーザ権限、フクキタル権限
        final MUserEntity user = createUserCanFukukitaruRoleJUN();

        // 検索条件
        final ScreenSettingFukukitaruSearchConditionModel model = new ScreenSettingFukukitaruSearchConditionModel();
        model.setListMasterType(new ArrayList<ScreenSettingFukukitaruMasterType>() {
            private static final long serialVersionUID = 1L;

            {
                add(ScreenSettingFukukitaruMasterType.INPUT_ASSIST_SET);
            }
        });
        model.setPartNoId(BigInteger.valueOf(7));
        model.setOrderId(BigInteger.valueOf(1));
        model.setDeliveryType(FukukitaruMasterDeliveryType.DOMESTIC);
        model.setInputAssistId(BigInteger.valueOf(1));

        // テスト実行
        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/screenSettings/fukukitaru/orders/bottomBill")
                        .with(csrf())
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(model)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();
        System.out.println(actual.getResponse().getContentAsString());

        // 期待値


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
