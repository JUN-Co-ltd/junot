package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import jp.co.jun.edi.entity.MFBrandEntity;

/**
 * フクキタル用ブランド情報を検索するリポジトリ.
 */
public interface MFBrandRepository extends JpaRepository<MFBrandEntity, BigInteger>, JpaSpecificationExecutor<MFBrandEntity>  {

}
