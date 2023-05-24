package jp.co.jun.edi.component.mail;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.entity.MKojmstEntity;
import jp.co.jun.edi.repository.AdrmstRepository;
import jp.co.jun.edi.repository.MKojmstRepository;
import jp.co.jun.edi.repository.MUserRepository;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.type.BooleanType;

/**
 * メールアドレス生成用コンポーネント.
 */
@Component
public class MailAddressComponent {
    private static final String COMMA = ",";
    @Autowired
    private MUserRepository mUserRepository;

    @Autowired
    private MKojmstRepository mKojmstRepository;

    @Autowired
    private TDeliveryDetailRepository tDeliveryDetailRepository;

    // PRD_0143 #10423 JFE add start
    @Autowired
    private AdrmstRepository adrmstRepository;
    // PRD_0143 #10423 JFE add end

    @Autowired
    private PropertyComponent propertyComponent;

    /**
     * 発注情報.生産メーカー に紐づく 工場マスタ.納品依頼書送付先メールアドレス１ のメールアドレスを取得.
     * アドレス取得できない場合は、空文字を返す。
     * {@link MailAddressComponent#getDeliveryMdfMakerFactoryMailaddressArray(BigInteger, String, String)}.
     * @param deliveryId 納品ID
     * @param sire 発注情報.生産メーカー
     * @param kojcd 発注情報.生産工場
     * @return メールアドレス
     */
    public String getDeliveryMdfMakerFactoryMailaddress(final BigInteger deliveryId, final String sire, final String kojcd) {
        return String.join(COMMA, getDeliveryMdfMakerFactoryMailaddressArray(deliveryId, sire, kojcd));
    }

    /**
     * 発注情報.生産メーカー に紐づく 工場マスタ.納品依頼書送付先メールアドレス１ のメールアドレスを取得.
     * アドレス取得できない場合は、空配列を返す。
     * 複数アドレスが指定されている場合は、カンマ区切りで返す。
     * <pre>
     * 1)発注情報の生産メーカーをキーに工場マスタのレコードを抽出
     *   発注情報.生産メーカー =
     *   工場マスタ.仕入先コード/工場コード／ＳＰＯＴ
     *   AND  発注情報.生産工場 = 工場マスタ.工場コード（スポットコード）
     *   AND 工場マスタ.メンテ区分 <> 3
     * 2)1で取得したレコードの納品依頼書送付先区分が3または8の場合かつ、納品情報に紐づく納品明細情報の先頭1件の
     *   ファックス送信フラグがtrueの場合は夜間送信を行う。
     * 3)2の条件に一致する工場マスタの納品依頼書送付先メールアドレス１に登録されているアドレス宛に納品依頼書を送付する
     * </pre>
     * @param deliveryId 納品ID
     * @param sire 発注情報.生産メーカー
     * @param kojcd 発注情報.生産工場
     * @return メールアドレス
     */
    public String[] getDeliveryMdfMakerFactoryMailaddressArray(final BigInteger deliveryId, final String sire, final String kojcd) {
        // 1)発注情報の生産メーカーをキーに工場マスタのレコードかつ、
        // 2)納品依頼書送付先区分が3または8のレコードを抽出
        final Optional<MKojmstEntity> optionalMKojmstEntity = mKojmstRepository.findBySireKojCd(sire, kojcd);
        if (optionalMKojmstEntity.isPresent()) {
            final MKojmstEntity mKojmstEntity = optionalMKojmstEntity.get();
            // 2)納品情報に紐づく納品明細情報の先頭1件のファックス送信フラグがtrueの場合
            if (tDeliveryDetailRepository.findByDeliveryId(deliveryId, PageRequest.of(0, 1, Sort.by(Order.asc("divisionCode"))))
                    .stream()
                    .filter(predicate -> predicate.getFaxSend() == BooleanType.TRUE)
                    .findFirst().isPresent()) {
                // 3)工場マスタの納品依頼書送付先メールアドレス１に登録されているアドレス宛を取得する
                return splitMailAddressList(Arrays.asList(mKojmstEntity.getNemail1()));
            }

        }
        // 条件に該当しないため空配列を返す
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }


    /**
     * 発注情報.生産メーカー に紐づく 工場マスタ.発注書送付先メールアドレス１ のメールアドレスを取得.
     * アドレス取得できない場合は、空文字を返す。
     * 複数アドレスが指定されている場合は、カンマ区切りで返す。
     * <pre>
     * 1)発注情報の生産メーカーをキーに工場マスタのレコードを抽出
     *   発注情報.生産メーカー =
     *   工場マスタ.仕入先コード/工場コード／ＳＰＯＴ
     *   AND  発注情報.生産工場 = 工場マスタ.工場コード（スポットコード）
     *   AND 工場マスタ.メンテ区分 <> 3
     * 2)1で取得したレコードの発注書送付先区分が3または7の場合は夜間送信を行う。
     * 3)2の条件に一致する工場マスタの発注書送付先メールアドレス１に登録されているアドレス宛に発注書を送付する。
     * </pre>
     * @param sire 発注情報.生産メーカー
     * @param kojcd 発注情報.生産工場
     * @return メールアドレス
     */
    public String getOrderMdfMakerFactoryMailaddress(final String sire, final String kojcd) {
        // 1)発注情報の生産メーカーをキーに工場マスタのレコードかつ、
        // 2)発注書送付先区分が3または8のレコードを抽出
        final Optional<MKojmstEntity> optionalMKojmstEntity = mKojmstRepository.findBySireKojCdHsofkbn(sire, kojcd);
        if (optionalMKojmstEntity.isPresent()) {
            final MKojmstEntity mKojmstEntity = optionalMKojmstEntity.get();
            // 3)工場マスタの発注書送付先メールアドレス１に登録されているアドレス宛を取得する
            final String[] address = splitMailAddressList(Arrays.asList(mKojmstEntity.getHemail1()));
            return String.join(COMMA, address);

        }
        // 条件に該当しないため空文字を返す
        return "";
    }

    /**
     * 生産メーカーに紐づく全アカウントのメールアドレスを取得する.
     * @param supplierCode メーカーコード
     * @return メールアドレスの配列
     */
    public String[] getAllMakerAccountMailaddress(final String supplierCode) {
        // 生産メーカー担当ID(ユーザーID)でアドレス取得
        final List<String> mailAddresssList = mUserRepository.findMailAddressByCompany(supplierCode);
        return splitMailAddressList(mailAddresssList);
    };


    /**
     * 生産メーカー担当のメールアドレスを取得する.
     * @param mdfMakerStaffId 生産メーカー担当
     * @return メールアドレスをカンマ区切りで区切った文字列
     */
    public String getMakerMailaddress(final BigInteger mdfMakerStaffId) {
        return String.join(COMMA, getMakerMailaddressArray(mdfMakerStaffId));
    }

    /**
     * 生産メーカー担当のメールアドレスを取得する.
     * @param mdfMakerStaffId 生産メーカー担当
     * @return メールアドレスの配列
     */
    public String[] getMakerMailaddressArray(final BigInteger mdfMakerStaffId) {
        // 生産メーカー担当ID(ユーザーID)でアドレス取得
        final String mailAddressStr = mUserRepository.findMailAddressById(mdfMakerStaffId);
        final String[] mailAddress = splitMailAddressList(Arrays.asList(mailAddressStr));

        return mailAddress;
    }

    /**
     * JUNの製造担当、企画担当、パタンナーのメールアドレスを取得する.
     * @param mdfStaffCode 製造担当
     * @param loginAccoutName ログインアカウント名
     * @return メールアドレスをカンマ区切りで区切った文字列
     */
    public String getJunMailaddress(final String mdfStaffCode, final String loginAccoutName) {
        return String.join(COMMA, getJunMailaddressArray(mdfStaffCode, loginAccoutName));
    }

    /**
     * JUNの製造担当、企画担当、パタンナーのメールアドレスを取得する.
     * @param mdfStaffCode 製造担当
     * @param loginAccoutName ログインアカウント名
     * @return メールアドレスの配列
     */
    public String[] getJunMailaddressArray(final String mdfStaffCode, final String loginAccoutName) {
        final Set<String> staffCodesSet = Stream.of(
                // 製造担当を設定
                mdfStaffCode)
                // メール送信対象のアカウントか判定する
                .filter(staffCode -> isSendMailAccount(staffCode, loginAccoutName))
                .collect(Collectors.toCollection(HashSet::new));

        if (staffCodesSet.isEmpty()) {
            return new String[0];
        }

        // アカウント名リストでアドレス取得
        final List<String> mailAddressList = mUserRepository
                .findMailAddressByAccountNameAndCompany(staffCodesSet, propertyComponent.getCommonProperty().getJunCompany());
        final String[] mailAddress = splitMailAddressList(mailAddressList);

        return mailAddress;
    }

    /**
     * メール送信対象のアカウントか判定する.
     * @param staffCode 担当者コード
     * @param loginAccoutName ログインアカウント名
     * @return 判定結果
     */
    private boolean isSendMailAccount(final String staffCode, final String loginAccoutName) {
        if (StringUtils.isBlank(staffCode)) {
            // NULL、空文字、空白の場合は、送信対象外
            return false;
        }

        if (!propertyComponent.getCommonProperty().isSendMailLoginUserSend()) {
            // ログインユーザーへ送信しない場合
            if (StringUtils.equals(staffCode, loginAccoutName)) {
                // ログインアカウント名と一致する場合、送信対象外
                return false;
            }
        }

        return true;
    }

    /**
     * メールアドレスをカンマで分割し、重複除去後配列につめる.
     * @param mailAddress 取得したメールアドレスのリスト
     * @return 分割後のメールアドレスの配列
     */
    private String[] splitMailAddressList(final List<String> mailAddress) {

        String[] emails = {};

        if (mailAddress.isEmpty()) {
            return emails;
        }

        for (String address : mailAddress) { // ;ではなくて:なので注意
            String[] email = StringUtils.split(address, COMMA);
            emails = ArrayUtils.addAll(emails, email);
        }

        // 重複したメールアドレスを除去
        final List<String> list = Arrays.asList(emails);
        final String[] splittedMailAddress = (String[]) new LinkedHashSet<>(list).toArray(new String[0]);

        return splittedMailAddress;
    }

    /**
     * メールアドレスの配列をカンマ結合し、重複除去後文字列で返却.
     * @param mailAddress 取得したメールアドレスのリスト
     * @return 結合後のメールアドレスの配列
     */
    public String joinMailAddressList(final String[] mailAddress) {
        return String.join(COMMA, mailAddress);
    }

    //PRD_0134 #10654 add JEF start
    /**
     * 仕入情報.仕入先 に紐づく 工場マスタ.予備 受領書送付先メールアドレス１ のメールアドレスを取得.
     * アドレス取得できない場合は、空文字を返す。
     * 複数アドレスが指定されている場合は、カンマ区切りで返す。
     * @param sire 仕入情報.仕入先コード
     * @param kojcd 発注情報.生産工場
     * @return メールアドレス
     */
    public String getPurchaseItemMailaddress(final String sire) {
        // 1)仕入情報の仕入先コードをキーに工場マスタのレコードかつ、
        // 2)受領書送付先区分が3または8のレコードを抽出
        final Optional<MKojmstEntity> optionalMKojmstEntity = mKojmstRepository.findBySireysofkbn(sire);
        if (optionalMKojmstEntity.isPresent()) {
            final MKojmstEntity mKojmstEntity = optionalMKojmstEntity.get();
            // 3)工場マスタの受領書送付先メールアドレス１に登録されているアドレス宛を取得する
            final String[] address = splitMailAddressList(Arrays.asList(mKojmstEntity.getYemail1()));
            return String.join(COMMA, address);
        }
        // 条件に該当しないため空文字を返す
        return "";
    }
    //PRD_0134 #10654 add JEF end
    //PRD_0143 #10423 add JFE start
    /**
     * TAGDAT送信先メールアドレスを取得する.
     * @param brkg ブランドコード
     * @return メールアドレスをカンマ区切りで区切った文字列
     */
    public String getTagdatMailaddress(final String brkg) {
        return String.join(COMMA, getTagdatMailaddressArray(brkg));
}

    /**
     * TAGDAT送信先メールアドレスを取得する.
     * @param brkg ブランドコード
     * @return メールアドレスの配列
     */
    public String[] getTagdatMailaddressArray(final String brkg) {
        // ブランドコードでアドレス取得
        final List<String> mailAddressList = adrmstRepository.findMailAddressByBrkg(brkg);
        final String[] mailAddress = splitMailAddressList(mailAddressList);

        return mailAddress;
    }
}
//PRD_0143 #10423 add JFE end
