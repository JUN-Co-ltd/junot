package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.entity.MNumberEntity;
import jp.co.jun.edi.repository.MNumberRepository;
import jp.co.jun.edi.type.MNumberColumnNameType;
import jp.co.jun.edi.type.MNumberTableNameType;
import jp.co.jun.edi.util.StringUtils;

/**
 * 採番マスタ関連のコンポーネント.
 */
@Component
public class MNumberComponent extends GenericComponent {

    @Autowired
    private MNumberRepository numberRepository;

    /**
     * 採番処理を行う.
     * @param tableName テーブル名
     * @param columnName カラム名
     * @return BigInteger
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public BigInteger createNumber(final MNumberTableNameType tableName, final MNumberColumnNameType columnName) {

        // 現在値（採番されている最大の値を取得）
        Optional<MNumberEntity> optional = numberRepository.findByTableNameAndColumnName(tableName.getValue(), columnName.getValue());

        // 取得できた場合
        if (optional.isPresent()) {

            final MNumberEntity numberEntity = optional.get();

            // インクリメントして最大値を超える場合、null返却
            if (numberEntity.getNowNumber().add(BigInteger.valueOf(1)).compareTo(numberEntity.getMaxNumber()) > 0) {
                return null;
            }

            // インクリメントした値を採番マスタに更新
            numberEntity.setNowNumber(numberEntity.getNowNumber().add(BigInteger.valueOf(1)));
            numberRepository.save(numberEntity);

            // 採番値を返却
            return numberEntity.getNowNumber();
        }

        return null;
    }


    /**
     * レコード追加型の採番処理を行う.
     * 例：納品SKUの納品依頼番号の採番処理
     * テーブル名＋課コードで検索し、レコードがあれば加算して返却.
     * レコードが取れなければ新規課として取り扱い、
     * デフォルトレコードをコピーして、採番テーブルへのレコード追加も行う。
     *
     * @param tableName tableName
     * @param defaultColumnName defaultColumnName
     * @param targetColumnName targetColumnName
     * @return BigInteger
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public BigInteger createNumberTypeAdditional(
            final MNumberTableNameType tableName,
            final MNumberColumnNameType defaultColumnName,
            final MNumberColumnNameType targetColumnName) {

        // ターゲットの現在値（採番されている最大の値を取得）
        final BigInteger returnNumber = createNumber(tableName, targetColumnName);

        if (returnNumber != null) {
            // 取れた場合はそのまま返却
            return returnNumber;
        }

        // 取得できない（新規）場合は、デフォルトレコードを取得し、コピーしてレコードを追加する
        final Optional<MNumberEntity> optional = numberRepository.findByTableNameAndColumnName(tableName.getValue(), defaultColumnName.getValue());

        // デフォルトが取得できた場合
        if (optional.isPresent()) {

            final MNumberEntity numberEntity = new MNumberEntity();

            // 項目のコピー
            numberEntity.setTableName(optional.get().getTableName());
            numberEntity.setNowNumber(optional.get().getNowNumber());
            numberEntity.setMinNumber(optional.get().getMinNumber());
            numberEntity.setMaxNumber(optional.get().getMaxNumber());
            // カラム名をターゲットに設定
            numberEntity.setColumnName(targetColumnName.getValue());

            // 更新
            numberRepository.save(numberEntity);

            // 採番値を返却
            return numberEntity.getNowNumber();
        }

        return null;
    }

    /**
     * 指定桁数まで前ゼロ埋めした番号の採番を行う.
     *
     * @param tableName テーブル名
     * @param columnName カラム名
     * @param length ゼロ埋めする文字列長
     * @return 指定のlengthまで前ゼロ埋めした番号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String createNumberSetZeroPadding(final MNumberTableNameType tableName, final MNumberColumnNameType columnName, final int length) {

        BigInteger number = createNumber(tableName, columnName);

        if (number == null) {
            return null;
        }

        String zeroPadNumber = StringUtils.toStringPadding0(number, length);

        return zeroPadNumber;

    }
}
