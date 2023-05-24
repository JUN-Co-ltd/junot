package jp.co.jun.edi.component;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.model.ErrorDetailModel;
import jp.co.jun.edi.type.MessageCodeType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class MessageComponentTest {
    /** カラーコード. */
    private static final String COLOR_CODE = "00";

    /** 組成コード. */
    private static final String COMPOSITION_CODE = "YG";

    /** パーツID. */
    private static final String PARTS_CODE = "1252";

    @Autowired
    private MessageComponent messageComponent;

    @Test
    public void test_get_0001() throws Exception {
        final List<ResultMessage> test = new ArrayList<>();

        // 品番エラー
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_03));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_04));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_05));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_06));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_07));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_08));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_09, COMPOSITION_CODE));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_10, PARTS_CODE));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_11));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_12));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_13));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_14));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_15));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_16));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_I_17));

        // 発注エラー
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_O_001));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_O_002));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_O_003));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_O_004));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_O_005));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_O_006));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_O_007));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_O_008));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_O_009));

        // 納品依頼エラー
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_001));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_002));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_003));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_004));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_005));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_006));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_007));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_008));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_009));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_010));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_011));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_012));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_013));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_014));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_015));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_016));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_017));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_018));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_019));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_020));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_021));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_022));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_D_023));

        // 納品予定エラー
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_P_001));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_P_002));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_P_003));

        // 優良誤認承認エラー
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_001));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_002));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_003));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_004));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_005));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_006, COLOR_CODE));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_007));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_008));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_009));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_010));

        // 生地検査エラー
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_M_001));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_M_002));

        // SQエラー
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_S_001));

        // ログインエラー
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_L_001));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_L_002));

        // フクキタルエラー
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_FO_001));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_FO_002));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_FO_003));

        // 納品依頼回数採番マスタデータ未登録
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_DN_001));

        // 共通エラー
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_002));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_003));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_004));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_005));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_006));
        test.add(ResultMessage.fromCode(MessageCodeType.CODE_007));

        // システムエラー
        test.add(ResultMessage.fromCode(MessageCodeType.SYSTEM_ERROR));

        final List<ErrorDetailModel> testMessages = messageComponent.toErrorDetails(test);
        testMessages.forEach(testMessage -> {
            System.out.println("code." + testMessage.getCode().convertToValue() + "=" + testMessage.getMessage());
        });
    }
}
