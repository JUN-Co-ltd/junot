// CHECKSTYLE:OFF

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
import jp.co.jun.edi.model.FukukitaruItemAttentionAppendicesTermModel;
import jp.co.jun.edi.model.FukukitaruItemModel;
import jp.co.jun.edi.model.FukukitaruItemWashAppendicesTermModel;
import jp.co.jun.edi.model.FukukitaruItemWashPatternModel;
import jp.co.jun.edi.type.BooleanType;
import test.utils.LoginUserUtils;

/**
 * MisleadingRepresentationCreateServiceテスト.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class FukukitaruItemV1ApiUpdateTest {
    // モックサーバー
    @Autowired
    private MockMvc mockServer;
    @Autowired
    private ObjectMapper objectMapper;
    /**
     * 観点1.各Repositoryに正しい値を渡せていることを確認する.
     * 観点2.Serviceが正しい値を返せていることを確認する(OUT).
     * 観点3.FileInfoMode.INSERT
     * @throws Exception
     */
    //@WithMockUser(username = "000000\n000001", password = "H3RYB9F8", roles = { "JUN", "USER" })
    @Test
    public void test_create_success() throws Exception {

        final MUserEntity user = createUser();
        final FukukitaruItemModel test = createFukukitaruItemModel();



        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.put("/api/v1/fukukitaru/items/4")
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(test)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();

        System.out.println( actual);

    }
    private FukukitaruItemModel createFukukitaruItemModel() {
        final FukukitaruItemModel model = new FukukitaruItemModel();
        // ID
        model.setId(BigInteger.valueOf(1));
        // 品番ID
        model.setPartNoId(BigInteger.valueOf(1));
        // カテゴリコード（VIS：空文字固定）
        model.setCategoryCode(BigInteger.valueOf(1));
        // NERGY用メリット下札コード1（VIS：空文字固定）
        model.setNergyBillCode1("b");
        // NERGY用メリット下札コード2（VIS：空文字固定）
        model.setNergyBillCode2("c");
        // NERGY用メリット下札コード3（VIS：空文字固定）
        model.setNergyBillCode3("d");
        // NERGY用メリット下札コード4（VIS：空文字固定）
        model.setNergyBillCode4("e");
        // NERGY用メリット下札コード5（VIS：空文字固定）
        model.setNergyBillCode5("f");
        // NERGY用メリット下札コード6（VIS：空文字固定）
        model.setNergyBillCode6("g");
        // シールへの付記用語印字
        model.setPrintAppendicesTerm(BooleanType.TRUE);
        // 原産国表記フラグ(0:表記しない,1:表記する)
        model.setPrintCoo(BooleanType.TRUE);
        // シールへの品質印字
        model.setPrintParts(BooleanType.TRUE);
        // QRコードの有無
        model.setPrintQrcode(BooleanType.TRUE);
        // 洗濯ネームサイズ印字
        model.setPrintSize(BooleanType.TRUE);
        // シールへの絵表示印字
        model.setPrintWashPattern(BooleanType.TRUE);
        // シールへのリサイクルマーク印字
        model.setRecycleMark(BigInteger.valueOf(14));
        // REEFUR用ブランド（VIS：空文字固定）
        model.setReefurPrivateBrandCode("h");
        // サタデーズサーフ用NY品番（VIS：空文字固定）
        model.setSaturdaysPrivateNyPartNo("i");
        // アテンションシールのシール種類
        model.setStickerTypeCode(BigInteger.valueOf(51));
        // 洗濯ネームテープ種類
        model.setTapeCode(BigInteger.valueOf(16));
        // 洗濯ネームテープ巾
        model.setTapeWidthCode(BigInteger.valueOf(17));
        // アテンションタグ用付記用語1
        model.setListItemAttentionAppendicesTerm(new ArrayList<FukukitaruItemAttentionAppendicesTermModel>() {
            private static final long serialVersionUID = 1L;

            {
                final FukukitaruItemAttentionAppendicesTermModel submodel = new FukukitaruItemAttentionAppendicesTermModel();
                submodel.setId(BigInteger.valueOf(3));
                submodel.setColorCode("01");
                submodel.setAppendicesTermId(BigInteger.valueOf(11));

                add(submodel);
            }
        });
        // 洗濯ネーム用付記用語1
        model.setListItemWashAppendicesTerm(new ArrayList<FukukitaruItemWashAppendicesTermModel>() {
            private static final long serialVersionUID = 1L;

            {
                final FukukitaruItemWashAppendicesTermModel submodel = new FukukitaruItemWashAppendicesTermModel();
                submodel.setId(BigInteger.valueOf(3));
                submodel.setColorCode("02");
                submodel.setAppendicesTermId(BigInteger.valueOf(12));

                add(submodel);
            }
        });
        // 絵表示
        model.setListItemWashPattern(new ArrayList<FukukitaruItemWashPatternModel>() {
            private static final long serialVersionUID = 1L;

            {
                final FukukitaruItemWashPatternModel submodel = new FukukitaruItemWashPatternModel();
                submodel.setId(BigInteger.valueOf(1));
                submodel.setColorCode("03");
                submodel.setWashPatternId(BigInteger.valueOf(13));

                add(submodel);
            }
        });
        // サスティナブルマーク印字0：印字しない,1：印字する.
        model.setPrintSustainableMark(BooleanType.FALSE);
        return model;

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
}
//CHECKSTYLE:ON
