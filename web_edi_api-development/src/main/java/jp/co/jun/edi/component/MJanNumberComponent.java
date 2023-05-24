package jp.co.jun.edi.component;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.entity.MJanEntity;
import jp.co.jun.edi.entity.MJanNumberEntity;
import jp.co.jun.edi.entity.MUnusableJanEntity;
import jp.co.jun.edi.entity.TSkuEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.MJanNumberRepository;
import jp.co.jun.edi.repository.MJanRepository;
import jp.co.jun.edi.repository.MJanUseCompanyRepository;
import jp.co.jun.edi.repository.MUnusableJanRepository;
import jp.co.jun.edi.repository.TSkuRepository;
import jp.co.jun.edi.type.ChangeRegistStatusType;
import jp.co.jun.edi.type.JanType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.RegistStatusType;

/**
 * JANマスタのコンポーネント.
 */
@Component
public class MJanNumberComponent extends GenericComponent {

    @Autowired
    private MJanNumberRepository janNumberRepository;

    @Autowired
    private MJanRepository mJanRepository;

    @Autowired
    private MJanUseCompanyRepository mJanUseCompanyRepository;

    @Autowired
    private MUnusableJanRepository mUnusableJanRepository;

    @Autowired
    private TSkuRepository tSkuRepository;

    // チェックディジット無しJANの文字数
    private static final int NON_DIGIT_JAN_LENGTH = 12;

    // チェックディジット無し短縮JANの文字数
    private static final int NON_DIGIT_JAN_SHORTEN_LENGTH = 7;

    // チェックディジット無しUPCの文字数
    private static final int NON_DIGIT_UPC_LENGTH = 11;

    // チェックディジット有りUPCの文字数
    private static final int UPC_LENGTH = 12;

    // チェックディジット有りJANの文字数
    private static final int JAN_LENGTH = 13;

    // チェックディジット有り短縮タイプ（8桁）のJANの文字数
    private static final int JAN8_LENGTH = 8;

    // 倍数
    private static final int MULTIPLE_NUMBER = 3;

    // チェックディジット算出用
    private static final int DIGIT_NUMBER = 10;

    // 短縮タイプ（8桁）のJANの先頭ゼロの文字列（13桁 - 8桁 = 5桁）
    private static final String JAN8_ZERO_PADDING_STRING = "00000";

    // 短縮タイプ（8桁）のJANの先頭ゼロの文字数
    private static final int JAN8_ZERO_PADDING_LENGTH = JAN8_ZERO_PADDING_STRING.length();

    /**
     * 採番処理を行う.
     *
     * @param brandCode ブランドコード
     * @return BigInteger
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String createJan(final String brandCode) {
        // JAN使用会社マスタからJANマスタIDのリストを取得
        final List<BigInteger> janIds = mJanUseCompanyRepository.getJanIdsByBrandCode(brandCode);

        // JANマスタIDのリストが取得できない場合、システムエラー
        if (CollectionUtils.isEmpty(janIds)) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.SYSTEM_ERROR));
        }

        // JAN採番マスタから、未採番のJANの最小のIDを取得（取得できない場合、システムエラー）
        // 該当するレコードをロックする
        final BigInteger minId = janNumberRepository.getMinId(janIds).orElseThrow(
                () -> new BusinessException(ResultMessages.warning().add(MessageCodeType.SYSTEM_ERROR)));

        // 最小のIDをもとにJAN採番マスタのレコードを取得（取得できない場合、システムエラー）
        final MJanNumberEntity janNumberEntity = janNumberRepository.findById(minId).orElseThrow(
                () -> new BusinessException(ResultMessages.warning().add(MessageCodeType.SYSTEM_ERROR)));

        // チェックディジットを発行する
        final String checkDigit = calcCheckDigitJan(janNumberEntity.getNonDigitJanCode());

        final String articleNumber = StringUtils.join(janNumberEntity.getNonDigitJanCode(), checkDigit);

        // JUNoT内のSKUの中で重複があったらエラー
        if (tSkuRepository.findByJanCode(articleNumber).isPresent()) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.SYSTEM_ERROR));
        }

        // チェックディジットと使用済フラグを更新する
        janNumberEntity.setCheckDigit(checkDigit);
        janNumberEntity.setUsedFlg(true);

        janNumberRepository.save(janNumberEntity);

        // 採番値を返却
        return articleNumber;
    }

    /**
     * 13桁もしくは8桁のJANコード用チェックディジット発行.
     *
     * @param jan チェックディジット無しJANコード
     * @return チェックディジット
     */
    private String calcCheckDigitJan(final String jan) {

        if (!isJanLength(jan)) {
            return StringUtils.EMPTY;
        }

        // 12桁まで"0"埋め(JAN8対応)
        final String janCode = padZero(jan, NON_DIGIT_JAN_LENGTH);
        // CheckDigit計算
        int sum = 0;
        for (int i = 0; i < janCode.length(); i++) {
            int now = Integer.parseInt(janCode.substring(i, i + 1));
            if (i % 2 == 0) {
                sum += now;
            } else {
                sum += MULTIPLE_NUMBER * now;
            }
        }
        int cd = DIGIT_NUMBER - (sum % DIGIT_NUMBER);
        if (cd == DIGIT_NUMBER) {
            cd = 0;
        }

        return String.valueOf(cd);
    }

    /**
     * 12桁のUPCコード用チェックディジット発行.
     *
     * @param jan チェックディジット無しJANコード
     * @return チェックディジット
     */
    private String calcCheckDigitUPC(final String jan) {

        // 空文字 もしくは11桁以外の場合は空文字を返却
        if (!isNonDigitUpcLength(jan)) {
            return StringUtils.EMPTY;
        }

        // CheckDigit計算
        int sum = 0;
        for (int i = 0; i < jan.length(); i++) {
            int now = Integer.parseInt(jan.substring(i, i + 1));
            if (i % 2 == 0) {

                sum += MULTIPLE_NUMBER * now;
            } else {
                sum += 1 * now;
            }
        }
        int cd = DIGIT_NUMBER - (sum % DIGIT_NUMBER);
        if (cd == DIGIT_NUMBER) {
            cd = 0;
        }

        return String.valueOf(cd);
    }


    /**
     * JAN/UPCのゼロ埋め処理.
     *
     * @param text 文字列
     * @param length ゼロ埋めする桁数
     * @return 指定桁数まで前ゼロ埋めした文字列
     */
    private String padZero(final String text, final int length) {
        return StringUtils.leftPad(text, length, '0');
    }

    /**
     * チェックディジットチェック.
     * 正しいチェックディジットが設定されているかどうかを確認する.
     *
     * @param articleNumber チェックディジット付きのJAN/UPC
     * @return true:正しいチェックディジット false:不正なチェックディジット
     */
    public boolean isCorrectCheckDigit(final String articleNumber) {

        if (StringUtils.isEmpty(articleNumber)) {
            return false;
        }

        final int articleNumberLength = StringUtils.length(articleNumber);
        final int nonDigitarticleNumberLength = articleNumberLength - 1;
        final String inputCheckDigit = StringUtils.right(articleNumber, 1);
        final String nonDigitarticleNumber = StringUtils.left(articleNumber, articleNumberLength - 1);

        String checkDigit = StringUtils.EMPTY;

        // チェックデジット取得
        switch (nonDigitarticleNumberLength) {

        case NON_DIGIT_JAN_LENGTH:
        case NON_DIGIT_JAN_SHORTEN_LENGTH:
            checkDigit = calcCheckDigitJan(nonDigitarticleNumber);
            break;

        case NON_DIGIT_UPC_LENGTH:
            checkDigit = calcCheckDigitUPC(nonDigitarticleNumber);
            break;

        default:
            break;

        }

        if (StringUtils.equals(checkDigit, inputCheckDigit)) {
            return true;
        }

        return false;
    }

    /**
     * JAN登録済チェック.
     *
     * @param id skuのID
     * @param articleNumber チェックディジット付きのJAN/UPC
     * @return true: 登録済 false:未登録
     */
    public boolean isRegistered(final BigInteger id, final String articleNumber) {

        if (StringUtils.isEmpty(articleNumber)) {
            return false;
        }

        // JAN使用不可マスタに登録が無いかチェック
        final Optional<MUnusableJanEntity> unsableJan = mUnusableJanRepository.findByJanCode(articleNumber);

        if (unsableJan.isPresent()) {
            return true;
        }

        // SKUの中で重複が無いかチェック
        final Optional<TSkuEntity> registedSku = tSkuRepository.findByJanCode(articleNumber);

        if (registedSku.isPresent() && !ObjectUtils.equals(id, registedSku.get().getId())) {
            return true;
        }

        return false;
    }

    /**
     * 重複チェック.
     * 引数内でJANの重複の有無を確認する.
     *
     * @param articleNumber 確認対象のJAN/UPCコード
     * @param articleNumberList チェックディジット付きのJAN/UPCのリスト
     * @return true: 重複あり false:重複なし
     */
    public boolean isDuplicate(final String articleNumber, final List<String> articleNumberList) {

        final long count = articleNumberList.stream().filter(articleNum -> StringUtils.equals(articleNum, articleNumber)).count();

        if (count > 1) {
            return true;
        }

        return false;

    }

    /**
     * JANが社内発行分かどうかチェック.
     *
     * @param janCode JANコード
     * @return true:社内発行分JAN false:社外発行分JAN またはJAN以外
     */
    public boolean isInternalRange(final String janCode) {

        if (StringUtils.isEmpty(janCode)) {
            return false;
        }

        // UPCの時はチェック不要
        if (StringUtils.length(janCode) == UPC_LENGTH) {
            return false;
        }

        // 国コード
        final int countryCode = NumberUtils.toInt(StringUtils.left(janCode, 2));
        // 事業者コード
        final int businessCode = NumberUtils.toInt(StringUtils.mid(janCode, 2, 5));

        final Page<MJanEntity> mJanList = mJanRepository.findByCountryCodeAndbusinessCode(countryCode,
                                                                                     businessCode,
                                                                                     PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id"))));


        if (mJanList.getTotalElements() == 0) {
            return false;
        }

        return true;

    }

    /**
     * JANコード規定の文字数かどうかを判定.
     *
     * @param janCode JANコード
     * @return true:規定の文字数 false:規定外の文字数
     */
    private boolean isJanLength(final String janCode) {

        if (StringUtils.isEmpty(janCode)) {
            return false;
        }

        return StringUtils.length(janCode) == NON_DIGIT_JAN_LENGTH || StringUtils.length(janCode) == NON_DIGIT_JAN_SHORTEN_LENGTH;

    }


    /**
     * チェックディジット無しのUPCコード規定の文字数かどうかを判定.
     *
     * @param upcCode UPCコード
     * @return true:規定の文字数 false:規定外の文字数
     */
    private boolean isNonDigitUpcLength(final String upcCode) {

        if (StringUtils.isEmpty(upcCode)) {
            return false;
        }

        return StringUtils.length(upcCode) == NON_DIGIT_UPC_LENGTH;

    }

    /**
     * JAN/UPCコードを前13桁ゼロ埋めする.
     *
     * <p>
     * SQが13桁のJAN/UPCしかデータを受け取れないため、前13桁ゼロ埋めしてJUNoTのDBに格納する。
     * 発注生産もDB上は13桁で保持している。
     * </p>
     *
     * <pre>
     * 以下の条件に該当する場合、前13桁ゼロ埋めする。
     * - 短縮タイプ（8桁）の他社JAN
     * - UPC
     * </pre>
     *
     * @param janType JAN区分
     * @param articleNumber JAN/UPCコード
     * @return ゼロ埋めしたJAN/UPCコード
     * @see jp.co.jun.edi.component.MJanNumberComponent#zeroSuppressArticleNumber zeroSuppressArticleNumber
     */
    public String zeroPaddingArticleNumber(final JanType janType, final String articleNumber) {
        if (StringUtils.isEmpty(articleNumber)) {
            // 空の場合、そのまま返却
            return articleNumber;
        }

        switch (janType) {
        case OTHER_JAN: // 他社JAN
        case OTHER_UPC: // 他社UPC
            // 13桁に満たない場合は前13桁ゼロ埋め
            return padZero(articleNumber, JAN_LENGTH);
        default:
            // そのまま返却
            return articleNumber;
        }
    }

    /**
     * 前ゼロ埋めされたJAN/UPCコードからゼロを取り除く.
     *
     * <pre>
     * 以下の条件に該当する場合、前ゼロ除去する。
     * - 先頭5桁がすべてゼロの他社JAN
     * - UPC
     * </pre>
     *
     * @param janType JAN区分
     * @param articleNumber JAN/UPCコード
     * @return ゼロサプレスしたJAN/UPCコード
     * @see jp.co.jun.edi.component.MJanNumberComponent#zeroPaddingArticleNumber zeroPaddingArticleNumber
     */
    public String zeroSuppressArticleNumber(final JanType janType, final String articleNumber) {
        if (StringUtils.isEmpty(articleNumber)) {
            // 空の場合、そのまま返却
            return articleNumber;
        }

        switch (janType) {
        case OTHER_JAN: // 他社JAN
            if (JAN8_ZERO_PADDING_STRING.equals(StringUtils.left(articleNumber, JAN8_ZERO_PADDING_LENGTH))) {
                // 先頭5桁がゼロの場合、短縮タイプ（8桁）とみなし、13桁→8桁に前ゼロを除去
                return StringUtils.right(articleNumber, JAN8_LENGTH);
            }
            // そのまま返却
            return articleNumber;
        case OTHER_UPC: // 他社UPC
            // 13桁→12桁に前ゼロを除去
            return StringUtils.right(articleNumber, UPC_LENGTH);
        default:
            // そのまま返却
            return articleNumber;
        }
    }

    /**
     * 自社JAN採番要否判定.
     *
     * <p>
     * 以下の条件にすべて該当する場合、自社JANの採番対象とする.
     * </p>
     * <pre>
     * - JAN区分 = 自社JAN
     * - ( 登録ステータス = 品番 OR 登録ステータス変更区分 = 品番登録 )
     * - JANコード = 空
     * </pre>
     *
     * @param janType JAN区分
     * @param registStatus 登録ステータス
     * @param changeRegistStatusType 登録ステータス変更区分
     * @param janCode JANコード
     * @return true:JAN採番を行う false:JAN採番を行わない
     */
    public boolean isInHouseJanNumbering(
            final JanType janType,
            final RegistStatusType registStatus,
            final ChangeRegistStatusType changeRegistStatusType,
            final String janCode) {
        return JanType.IN_HOUSE_JAN == janType
                && ((RegistStatusType.PART == registStatus)
                        || (ChangeRegistStatusType.PART == changeRegistStatusType))
                && StringUtils.isEmpty(janCode);
    }
}
