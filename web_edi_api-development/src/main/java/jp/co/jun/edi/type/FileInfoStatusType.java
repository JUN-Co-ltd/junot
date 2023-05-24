package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * ファイル状態の定義.
 */
public enum FileInfoStatusType {
    /** ファイル未処理(0). */
    FILE_UNPROCESSED(0),
    /** ファイル処理中(1). */
    FILE_PROCESSING(1),
    /** ファイル処理済(2). */
    FILE_COMPLETED(2),
    /** ファイル処理エラー(3). */
    FILE_ERROR(3);

    /**
     * value.
     */
    private final int value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    FileInfoStatusType(final int value) {
        this.value = value;
    }

    /**
     * valueを取得する.
     *
     * @return value
     */
    public int getValue() {
        return value;
    }

    /**
     * valueからtypeを検索する.
     *
     * @param value value
     * @return Optional<SendMailStatusType>
     */
    public static Optional<FileInfoStatusType> findByValue(final int value) {
        return Arrays.stream(values()).filter(v -> v.value == value).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final FileInfoStatusType type) {
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
    public static FileInfoStatusType convertToType(final Integer value) {
        if (Objects.isNull(value)) {
            return null;
        }

        return findByValue(value).orElse(null);
    }
    /**
     * [JSON変換用] typeを取得する.
     *
     * @param value value
     * @return ResultType
     */
    @JsonCreator
    public static FileInfoStatusType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        Integer valInt = Integer.parseInt(value);
        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(value));
    }
}
