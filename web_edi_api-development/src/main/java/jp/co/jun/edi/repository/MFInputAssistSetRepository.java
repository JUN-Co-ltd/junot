package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import jp.co.jun.edi.entity.MFInputAssistSetEntity;

/**
 * フクキタル用入力補助セットマスタを検索するリポジトリ.
 */
public interface MFInputAssistSetRepository extends JpaRepository<MFInputAssistSetEntity, BigInteger>, JpaSpecificationExecutor<MFInputAssistSetEntity> {

}
