package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 権限の定義.
 * <p>
 * マスタメンテナンスのユーザ検索画面で、ユーザマスタの権限に対し、LIKE検索をしているので、権限の命名には注意すること。
 * </p>
 */
public enum AuthorityType {
    /** ROLE_USER : ユーザ権限(APIへのアクセス制限のために利用). */
    ROLE_USER("ROLE_USER"),
    /** ROLE_ADMIN : 管理者権限(APIへのアクセス制限とマスタメンテナンス制限のために利用). */
    ROLE_ADMIN("ROLE_ADMIN"),
    /** ROLE_JUN : JUN権限. */
    ROLE_JUN("ROLE_JUN"),
    /** ROLE_QA : QAセンター権限. */
    ROLE_QA("ROLE_QA"),
    /** ROLE_MAKER : メーカー権限. */
    ROLE_MAKER("ROLE_MAKER"),
    /** ROLE_DISTA : 倉庫関係の機能操作権限. */
    ROLE_DISTA("ROLE_DISTA"),
    /** ROLE_EDI : JUNoTのEDI操作権限. */
    ROLE_EDI("ROLE_EDI");

    /**
     * value.
     */
    private final String value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    AuthorityType(final String value) {
        this.value = value;
    }

    /**
     * valueを取得する.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * valueからtypeを検索する.
     *
     * @param value value
     * @return Optional<EnabledType>
     */
    public static Optional<AuthorityType> findByValue(final String value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static String convertToValue(final AuthorityType type) {
        if (Objects.isNull(type)) {
            return null;
        }

        return type.getValue();
    }

    /**
     * value を type に変換する. value が null の場合、null を返却する.
     *
     * @param value value
     * @return type
     */
    public static AuthorityType convertToType(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        return findByValue(value).orElse(null);
    }

    /**
     * [JSON変換用] valueを取得する.
     *
     * @return int
     */
    @JsonValue
    public String convertToValue() {
        return getValue();
    }

    /**
     * [JSON変換用] typeを取得する.
     *
     * @param value value
     * @return ResultType
     */
    @JsonCreator
    public static AuthorityType convertToJson(final String value) {
        return findByValue(value).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
