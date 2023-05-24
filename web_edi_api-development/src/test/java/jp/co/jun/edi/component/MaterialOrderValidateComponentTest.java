package jp.co.jun.edi.component;


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.jun.edi.component.materialorder.MaterialOrderValidateComponent;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.model.FukukitaruOrderModel;
import jp.co.jun.edi.model.FukukitaruOrderSkuModel;
import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.DateUtils;
/**
 *
 * テスト用カレンダー
 * <pre>
 * | 月日  | 日本 | 中国 |
 * | ----- | ---- | ---- |
 * | 04/03 |      |      |
 * | 04/04 | 休   | 休   |
 * | 04/05 | 休   | 休   |
 * | 04/06 |      | 休   |
 * | 04/07 |      |      |
 *     :
 * | 04/29 |      |      |
 * | 04/30 |      |      |
 * | 05/01 |      | 休   |
 * | 05/02 | 休   | 休   |
 * | 05/03 | 休   | 休   |
 * | 05/04 |      | 休   |
 * | 05/05 |      | 休   |
 * | 05/06 |      |      |
 * | 05/07 |      |      |
 * | 05/08 |      |      |
 * | 05/09 | 休   |      |
 * | 05/10 | 休   | 休   |
 * </pre>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class MaterialOrderValidateComponentTest {
    @Autowired
    private MaterialOrderValidateComponent component;

    @Test
    public void test希望出荷日バリデーション_希望出荷日が発注日より前を指定されている() throws Exception {
        // 内部クラスのValidatorをリフレクションしてインスタンスを生成する
        Class<MaterialOrderValidateComponent.Validator> cl = MaterialOrderValidateComponent.Validator.class;
        Constructor<MaterialOrderValidateComponent.Validator> constructor = cl.getConstructor(MaterialOrderValidateComponent.class);
        MaterialOrderValidateComponent.Validator validator = constructor.newInstance(component); // Validatorインスタンス生成

        // テスト対象のプライベートメソッドのアクセスを許可する
        Method method = MaterialOrderValidateComponent.Validator.class.getDeclaredMethod("isNonBusinessDay");
        method.setAccessible(true);

        // テストデータモデル生成
        final FukukitaruOrderModel model = new FukukitaruOrderModel();
        model.setOrderAt(DateUtils.stringToDate("2020/03/01"));
        model.setPreferredShippingAt(DateUtils.stringToDate("2020/02/29"));

        // テスト対象メソッドを実行
        method.invoke(validator.locale(Locale.JAPANESE).materialOrder(model));

        // 結果
        final List<ResultMessage> errors = validator.getResultMessages();
        // エラーが入っていること
        assertThat(errors.size(),is(1));
        // エラーがT1であること
        assertThat(errors.get(0).getCode(),is(MessageCodeType.CODE_FO_010));
    }

    @Test
    public void test希望出荷日バリデーション_希望出荷日が休業日() throws Exception {
        // 内部クラスのValidatorをリフレクションしてインスタンスを生成する
        Class<MaterialOrderValidateComponent.Validator> cl = MaterialOrderValidateComponent.Validator.class;
        Constructor<MaterialOrderValidateComponent.Validator> constructor = cl.getConstructor(MaterialOrderValidateComponent.class);
        MaterialOrderValidateComponent.Validator validator = constructor.newInstance(component); // Validatorインスタンス生成

        // テスト対象のプライベートメソッドのアクセスを許可する
        Method method = MaterialOrderValidateComponent.Validator.class.getDeclaredMethod("isNonBusinessDay");
        method.setAccessible(true);

        // テストデータモデル生成
        final FukukitaruOrderModel model = new FukukitaruOrderModel();
        model.setDeliveryType(FukukitaruMasterDeliveryType.DOMESTIC);
        model.setOrderAt(DateUtils.stringToDate("2020/03/01"));
        model.setPreferredShippingAt(DateUtils.stringToDate("2020/03/01"));

        // テスト対象メソッドを実行
        method.invoke(validator.locale(Locale.JAPANESE).materialOrder(model));

        // 結果
        final List<ResultMessage> errors = validator.getResultMessages();
        // エラーが入っていること
        assertThat(errors.size(),is(1));
        // エラーがT1であること
        assertThat(errors.get(0).getCode(),is(MessageCodeType.CODE_FO_011));
    }

    /**
     * 希望出荷日が最短出荷可能日より前に指定されているためエラーとなることを確認する
     * <pre>
     *
     * 条件
     *   緊急出荷：なし
     *   発注日：2020/05/01 （営業日）
     *   希望出荷日：2020/05/01 （休業日）
     *   印字物：あり
     *   出荷カレンダ：中国
     * 結果
     *   最短出荷可能日：2020/05/06
     * </pre>
     * @throws Exception
     */
    @Test
    public void test希望出荷日バリデーション_希望出荷日が最短出荷可能日より前に指定されているためエラーとなることを確認する() throws Exception {
        // 内部クラスのValidatorをリフレクションしてインスタンスを生成する
        Class<MaterialOrderValidateComponent.Validator> cl = MaterialOrderValidateComponent.Validator.class;
        Constructor<MaterialOrderValidateComponent.Validator> constructor = cl.getConstructor(MaterialOrderValidateComponent.class);
        MaterialOrderValidateComponent.Validator validator = constructor.newInstance(component); // Validatorインスタンス生成

        // テスト対象のプライベートメソッドのアクセスを許可する
        Method method = MaterialOrderValidateComponent.Validator.class.getDeclaredMethod("isNonBusinessDay");
        method.setAccessible(true);

        // テストデータモデル生成
        final FukukitaruOrderModel model = new FukukitaruOrderModel();
        model.setDeliveryType(FukukitaruMasterDeliveryType.OVERSEES);
        model.setOrderAt(DateUtils.stringToDate("2020/04/04"));
        model.setPreferredShippingAt(DateUtils.stringToDate("2020/04/07"));
        model.setOrderSkuWashName(new ArrayList<FukukitaruOrderSkuModel>() {
            private static final long serialVersionUID = 1L;
            {
                add(new FukukitaruOrderSkuModel());
            }
        });


        // テスト対象メソッドを実行
        method.invoke(validator.locale(Locale.JAPANESE).materialOrder(model));

        // 結果
        final List<ResultMessage> errors = validator.getResultMessages();
        // エラーが入っていること
        assertThat(errors.size(),is(1));
        // エラーがT1であること
        assertThat(errors.get(0),is(MessageCodeType.CODE_FO_012));
    }
}
