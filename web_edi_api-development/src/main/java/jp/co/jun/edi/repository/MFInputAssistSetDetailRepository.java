package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import jp.co.jun.edi.entity.MFInputAssistSetDetailEntity;

/**
 * フクキタル用入力補助詳細セットマスタを検索するリポジトリ.
 */
public interface MFInputAssistSetDetailRepository
        extends JpaRepository<MFInputAssistSetDetailEntity, BigInteger>, JpaSpecificationExecutor<MFInputAssistSetDetailEntity> {

}
