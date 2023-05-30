import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

import { GenericList } from '../model/generic-list';
import { JunpcCodmstSearchCondition } from '../model/junpc-codmst-search-condition';
import { JunpcCodmstType } from '../model/junpc-codmst-type';
import { JunpcCodmst } from '../model/junpc-codmst';
import { BrandCode } from '../model/brand-code';
import { BrandCodesSearchConditions } from '../model/brand-codes-search-conditions';

import { JunotApiService } from '../service/junot-api.service';
import { fetchDistributionSectionsMock } from './mocks/junpc-codmst-service.mock';

const BASE_URL = '/junpc/codmst';
const BRAND_CODES_URL = `${ BASE_URL }/brandCodes`;
const COLORS_URL = `${ BASE_URL }/colors`;
const COMPOSITIONS_URL = `${ BASE_URL }/compositions`;
const MARUI_ITEMS_URL = `${ BASE_URL }/maruiItems`;
const ITEMS_URL = `${ BASE_URL }/items`;
const MATERIALS_URL = `${ BASE_URL }/materials`;
const ORIGIN_COUNTRIES_URL = `${ BASE_URL }/originCountries`;
const OUTLETS_URL = `${ BASE_URL }/outlets`;
const SEASONS_URL = `${ BASE_URL }/seasons`;
const SIZES_URL = `${ BASE_URL }/sizes`;
const STAFFS_URL = `${ BASE_URL }/staffs`;
const SUB_BRANDS_URL = `${ BASE_URL }/subBrands`;
const BRANDS_URL = `${ BASE_URL }/brands`;
const TASTES_URL = `${ BASE_URL }/tastes`;
const TYPES_URL = `${ BASE_URL }/types`;
const VOI_SECTIONS_URL = `${ BASE_URL }/voiSections`;
const ZONES_URL = `${ BASE_URL }/zones`;
const ALLOCATIONS_URL = `${ BASE_URL }/allocations`;
const DIVISIONS_URL = `${ BASE_URL }/divisions`;
const EXPENSE_ITEMS_URL = `${ BASE_URL }/expenseItems`;
const DISTRIBUTION_SECTION_URL = `${ BASE_URL }/distributionSections`;

/**
 * 発注生産システムのコードマスタを取得するService。
 */
@Injectable({
  providedIn: 'root'
})
export class JunpcCodmstService {
  private compositionsCache: BehaviorSubject<GenericList<JunpcCodmst>> = new BehaviorSubject(null);
  private materialsCache: BehaviorSubject<GenericList<JunpcCodmst>> = new BehaviorSubject(null);
  private originCountriesCache: BehaviorSubject<GenericList<JunpcCodmst>> = new BehaviorSubject(null);
  private voiSectionsCache: BehaviorSubject<GenericList<JunpcCodmst>> = new BehaviorSubject(null);

  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * ブランドコードをリストで取得します。
   * @param searchCondition 検索条件
   * @return ブランドコードのリスト
   */
  getBrandCodes(searchCondition?: BrandCodesSearchConditions): Observable<GenericList<BrandCode>> {
    return this.junotApiService.list(BRAND_CODES_URL, searchCondition);
  }

  /**
   * カラー情報をリストで取得します。
   *
   * @param searchCondition 検索条件
   * - searchType 検索区分 (任意)
   * - - 0 : コード/名称 部分一致（デフォルト）
   * - - 1 : コード 部分一致
   * - - 2 : 名称 部分一致
   * - searchText 検索文字列 (必須)
   * @return カラー情報のリスト
   */
  getColors(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(COLORS_URL, searchCondition);
  }

  /**
   * 組成情報をリストで取得します。
   *
   * @return 組成情報のリスト
   */
  getCompositions(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(COMPOSITIONS_URL, searchCondition);
  }

  /**
   * キャッシュから組成情報をリストで取得します。
   *
   * @return 組成情報のリスト
   */
  getCompositionsFromCache(): Observable<GenericList<JunpcCodmst>> {
    if (this.compositionsCache.getValue() != null) {
      return this.compositionsCache;
    }

    return this.getCompositions({ maxResults: 1000 } as JunpcCodmstSearchCondition).pipe(tap(x => this.compositionsCache.next(x)));
  }

  /**
   * 丸井品番情報をリストで取得します。
   *
   * @param searchCondition 検索条件
   * - brand ブランド (必須)
   * - item 内容１ (必須)
   * @return 丸井品番情報のリスト
   */
  getMaruiItems(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(MARUI_ITEMS_URL, searchCondition);
  }

  /**
   * アイテム情報をリストで取得します。
   *
   * @param searchCondition 検索条件
   * - brand ブランド (必須)
   * - item 内容１ (必須)
   * @return アイテム情報のリスト
   */
  getItems(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(ITEMS_URL, searchCondition);
  }

  /**
   * 素材情報をリストで取得します。
   *
   * @return 素材情報のリスト
   */
  getMaterials(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(MATERIALS_URL, searchCondition);
  }

  /**
   * キャッシュから素材情報をリストで取得します。
   *
   * @return 素材情報のリスト
   */
  getMaterialsFromCache(): Observable<GenericList<JunpcCodmst>> {
    if (this.materialsCache.getValue() != null) {
      return this.materialsCache;
    }

    return this.getMaterials({ maxResults: 1000 } as JunpcCodmstSearchCondition).pipe(tap(x => this.materialsCache.next(x)));
  }

  /**
   * 原産地情報をリストで取得します。
   *
   * @return 原産地情報のリスト
   */
  getOriginCountries(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(ORIGIN_COUNTRIES_URL, searchCondition);
  }

  /**
   * キャッシュから原産地情報をリストで取得します。
   *
   * @return 原産地情報のリスト
   */
  getOriginCountriesFromCache(): Observable<GenericList<JunpcCodmst>> {
    if (this.originCountriesCache.getValue() != null) {
      return this.originCountriesCache;
    }

    return this.getOriginCountries({ maxResults: 1000 } as JunpcCodmstSearchCondition).pipe(tap(x => this.originCountriesCache.next(x)));
  }

  /**
   * 展開情報をリストで取得します。
   *
   * @param searchCondition 検索条件
   * - brand ブランド (必須)
   * @return 展開情報のリスト
   */
  getOutlets(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(OUTLETS_URL, searchCondition);
  }

  /**
   * シーズン情報をリストで取得します。
   *
   * @return シーズン情報のリスト
   */
  getSeasons(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(SEASONS_URL, searchCondition);
  }

  /**
   * サイズ情報をリストで取得します。
   *
   * @param searchCondition 検索条件
   * - kind 種類 (必須)
   * @return サイズ情報のリスト
   */
  getSizes(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(SIZES_URL, searchCondition);
  }

  /**
   * 社員情報をリストで取得します。
   *
   * @param searchCondition 検索条件
   * - searchType 検索区分 (任意)
   * - - 0 : コード/名称 部分一致（デフォルト）
   * - - 1 : コード 部分一致
   * - - 2 : 名称 部分一致
   * - - 3 : コード 完全一致
   * - searchText 検索文字列 (必須)
   * @return 社員情報のリスト
   */
  getStaffs(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(STAFFS_URL, searchCondition);
  }

  /**
   * 社員情報を取得します。
   * @param accoutName アカウント名
   * @return 社員情報
   */
  getStaff(accoutName: string): Observable<JunpcCodmst> {
    const URL = `${ STAFFS_URL }/${ accoutName }`;
    return this.junotApiService.get(URL);
  }

  /**
   * サブブランド情報をリストで取得します。
   *
   * @param searchCondition 検索条件
   * - brand ブランド (必須)
   * @return サブブランド情報のリスト
   */
  getSubBrands(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(SUB_BRANDS_URL, searchCondition);
  }

  /**
   * テイスト情報をリストで取得します。
   *
   * @param searchCondition 検索条件
   * - brand ブランド (必須)
   * @return テイスト情報のリスト
   */
  getTastes(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(TASTES_URL, searchCondition);
  }

  /**
   * タイプ情報をリストで取得します。
   *
   * @param searchCondition 検索条件
   * - searchText 検索文字列 (必須)
   * @return タイプ情報のリスト
   */
  getTypes(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmstType>> {
    return this.junotApiService.list(TYPES_URL, searchCondition);
  }

  /**
   * Voi区分情報をリストで取得します。
   *
   * @return Voi区分情報のリスト
   */
  getVoiSections(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(VOI_SECTIONS_URL, searchCondition);
  }

  /**
   * キャッシュからVoi区分情報をリストで取得します。
   *
   * @return Voi区分情報のリスト
   */
  getVoiSectionsFromCache(): Observable<GenericList<JunpcCodmst>> {
    if (this.voiSectionsCache.getValue() != null) {
      return this.voiSectionsCache;
    }

    return this.getVoiSections({ maxResults: 1000 } as JunpcCodmstSearchCondition).pipe(tap(x => this.voiSectionsCache.next(x)));
  }

  /**
   * ゾーン情報をリストで取得します。
   *
   * @param searchCondition 検索条件
   * - brand ブランド (必須)
   * @return ゾーン情報のリスト
   */
  getZones(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(ZONES_URL, searchCondition);
  }

  /**
   * 配分課情報をリストで取得します。
   *
   * @param searchCondition 検索条件
   * - brand ブランド (必須)
   * @return 配分課情報のリスト
   */
  getAllocations(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(ALLOCATIONS_URL, searchCondition);
  }

  /**
   * 配分課情報から課コードを取得.
   * @param allocation 配分課情報
   * @returns 課コード
   */
  getDivisionCode(allocation: JunpcCodmst): string {
    // 課コード(配分課テーブルcode1の末尾2桁)
    return allocation.code1.substring(2, 4);
  }

  /**
   * 配分課情報から場所コードを取得.
   * @param allocation 配分課情報
   * @returns 場所コード
   */
  getAllocationCode(allocation: JunpcCodmst): string {
    // 場所コード(配分課テーブルitem3の先頭1桁)
    return allocation.item3.substring(0, 1);
  }

  /**
   * 配分課情報から物流コードを取得.
   * @param allocation 配分課情報
   * @returns 物流コード
   */
  getLogisticsCode(allocation: JunpcCodmst): string {
    // 物流コード(配分課テーブルitem3の先頭2桁)
    return allocation.item3.substring(0, 2);
  }

  /**
   * 全事業部情報をリストで取得します。
   * @return 事業部情報のリスト
   */
  getAllDivisions(): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(DIVISIONS_URL);
  }

  /**
   * @returns 配分課マスタリスト(tblid='85')
   */
  fetchDistributionSections(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.list(DISTRIBUTION_SECTION_URL, searchCondition);
  }

  /**
   * 費目情報を検索します。
   * @param searchCondition 検索条件
   * - code1 コード1(必須)
   * @return 費目情報のリスト
   */
  searchExpenseItems(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.listByPost(`${ EXPENSE_ITEMS_URL }/search`, searchCondition);
  }

  /**
   * ブランド情報を検索します。
   * @param searchCondition 検索条件
   * - brand ブランドコード (必須)
   * @return ブランド情報のリスト
   */
  searchBrands(searchCondition?: JunpcCodmstSearchCondition): Observable<GenericList<JunpcCodmst>> {
    return this.junotApiService.listByPost(`${ BRANDS_URL }/search`, searchCondition);
  }
}
