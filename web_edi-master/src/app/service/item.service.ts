import { Injectable } from '@angular/core';
import { NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';

import { ViewMode, Const, JanType } from '../const/const';
import { CodeMaster } from '../const/code-master';

import { StringUtils } from '../util/string-utils';
import { FormUtils } from '../util/form-utils';
import { ListUtils } from '../util/list-utils';

import { JunotApiService } from './junot-api.service';

import { GenericList } from '../model/generic-list';
import { Item } from '../model/item';
import { ItemSearchConditions } from '../model/search-conditions';
import { JunpcCodmst } from '../model/junpc-codmst';
import { Validate } from '../model/validate';
import { SkuFormValue } from '../interface/sku-form-value';

const BASE_URL = '/items';

/**
 * Item操作に関するService.
 */
@Injectable({
  providedIn: 'root'
})
export class ItemService {

  constructor(
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private junotApiService: JunotApiService
  ) { }

  /**
   * 品番情報登録処理.
   * @param postItem 品番データ
   * @returns Observable<Item>
   */
  postItem(postItem: any): Observable<Item> {
    const body = this.convertRequestData(postItem);
    return this.junotApiService.create(BASE_URL, body);
  }

  /**
   * 品番IDをキーに品番情報取得.
   * @param id 品番ID
   * @returns Observable<Item>
   */
  getItemForId(id: number): Observable<Item> {
    const URL = `${ BASE_URL }/${ id }`;
    return this.junotApiService.get(URL);
  }

  /**
   * 品番情報更新.
   * @param postItem 品番データ
   * @returns Observable<Item>
   */
  putItem(postItem): Observable<Item> {
    const URL = `${ BASE_URL }/${ postItem.id }`;
    const body = this.convertRequestData(postItem);
    return this.junotApiService.update(URL, body);
  }

  /**
   * 品番情報削除処理.
   * @param id 品番ID
   * @returns Observable<Item>
   */
  deleteItem(id: number): Observable<Item> {
    const URL = `${ BASE_URL }/${ id }`;
    return this.junotApiService.delete(URL);
  }

  /**
   * 品番情報取得処理.
   * @param searchConditions 品番検索条件
   * @returns Observable<GenericList<Item>>
   */
  getItemSearch(searchConditions: ItemSearchConditions): Observable<GenericList<Item>> {
    return this.junotApiService.list(BASE_URL, searchConditions);
  }

  /**
   * JAN/UPCのバリデーションチェック.
   * @param postItem 品番データ
   * @returns Observable<Validate>
   */
  validArticleNumber(postItem: Item): Observable<Validate> {
    const URL = `${ BASE_URL }/validArticleNumber`;
    const body = postItem;
    return this.junotApiService.create(URL, body);
  }

  /**
   * サブシーズンコードからシーズンコードを抽出.
   * @param サブシーズンコード
   * @returns シーズンコード
   */
  extractSeasonCode(subSeasonCode: string): string {
    if (StringUtils.isEmpty(subSeasonCode)) {
      return null;
    }

    let seasonCode = '';
    CodeMaster.subSeason.some(subSeason => {
      if (Number(subSeason.id) === Number(subSeasonCode)) {
        seasonCode = subSeason.value.slice(0, 1);
        return true;
      }
    });
    return seasonCode;
  }

  /**
   * データ整形処理
   * @param inputData 画面の入力データ
   * @returns 整形後入力データ
   */
  private convertRequestData(inputData: any): any {
    let copyItem = JSON.parse(JSON.stringify(inputData));

    // 品番の結合
    copyItem.partNo = StringUtils.defaultIfEmpty(copyItem.partNoKind, '') + StringUtils.defaultIfEmpty(copyItem.partNoSerialNo, '');

    // 金額のカンマ除去
    if (copyItem.retailPrice != null && typeof copyItem.retailPrice === 'string') {
      copyItem.retailPrice = copyItem.retailPrice.replace(/,/g, '');
    }
    if (copyItem.otherCost != null && typeof copyItem.otherCost === 'string') {
      copyItem.otherCost = copyItem.otherCost.replace(/,/g, '');
    }

    // SKUリストを送信用フォーマットに変更
    const reqColorList = [];
    // mainformのskusから色コードが入力されているもののみ抽出
    const colorList = copyItem.skus.filter(sku => sku.colorCode !== '');
    colorList.forEach(sku => {
      const selectedSizeList = sku.sizeList.filter(size => {
        size.colorCode = sku.colorCode;
        if (FormUtils.isEmpty(size.janCode)) {
          // JANコードが入力されていないものはJANコード初期化
          size.janCode = null;
        }
        return size.select === true; // チェックがついているもののみ抽出
      });
      // JANコードをセット
      this.setArticleNumberToSku(copyItem, selectedSizeList);
      Array.prototype.push.apply(reqColorList, selectedSizeList);
    });
    copyItem.skus = reqColorList;

    // 組成情報を送信用フォーマットに変換
    const compList = [];
    copyItem.compositions.forEach(colorCompList => {
      const activeConpList = colorCompList.compositionDetailList.filter(comp => {
        return StringUtils.isNotEmpty(comp.partsCode) ||
        StringUtils.isNotEmpty(comp.partsName) ||
        StringUtils.isNotEmpty(comp.compositionCode) ||
        StringUtils.isNotEmpty(comp.compositionName) ||
        StringUtils.isNotEmpty(comp.percent);
      });
      Array.prototype.push.apply(compList, activeConpList);
    });

    copyItem.compositions = compList;

    // 日付変換処理
    copyItem = this.convertDate(copyItem);

    return copyItem;
  }

  /**
   * SKUにJAN/UPCコードをセット
   * @param copyItem JSON型に変換した画面の入力データ
   * @param selectedSkuSizeList 選択済みのSKUサイズリスト
   */
  private setArticleNumberToSku(copyItem: any, selectedSkuSizeList: any): void {
    if (FormUtils.isEmpty(copyItem.articleNumbers)) { // JAN/UPCコードのFormが空の場合(商品情報画面)
      // 自社JANの場合、JAN/UPCコードがないものは全てnull。自社JAN以外は処理しない
      if (copyItem.janType === JanType.IN_HOUSE_JAN) {
        selectedSkuSizeList.filter(selectedSkuSize => FormUtils.isEmpty(selectedSkuSize.janCode))
          .forEach(emptyJanCodeSize => emptyJanCodeSize.janCode = null);
      }
      return;
    }

    selectedSkuSizeList.forEach(selectedSkuSize => {
      const sameColorSku = copyItem.articleNumbers.find(articleNumberSku => articleNumberSku.colorCode === selectedSkuSize.colorCode);
      const sameSize = sameColorSku.sizeList.find(size => size.size === selectedSkuSize.size);
      selectedSkuSize.janCode = sameSize.janCode;
    });
  }

  /**
   * 日付変換処理.
   * @param copyItem リクエストパラメータ
   * @returns 変換後リクエストパラメータ
   */
  private convertDate(copyItem: any): any {
    // 希望納品日
    if (copyItem.preferredDeliveryDate && typeof copyItem.preferredDeliveryDate !== 'string') {
      copyItem.preferredDeliveryDate = this.ngbDateParserFormatter.format(copyItem.preferredDeliveryDate).replace(/-/g, '/');
    }
    // 納品日
    if (copyItem.deliveryDate && typeof copyItem.deliveryDate !== 'string') {
      copyItem.deliveryDate = this.ngbDateParserFormatter.format(copyItem.deliveryDate).replace(/-/g, '/');
    }
    // 仮発注日
    if (copyItem.proviOrderDate && typeof copyItem.proviOrderDate !== 'string') {
      copyItem.proviOrderDate = this.ngbDateParserFormatter.format(copyItem.proviOrderDate).replace(/-/g, '/');
    }
    // 投入日
    if (copyItem.deploymentDate && typeof copyItem.deploymentDate !== 'string') {
      copyItem.deploymentDate = this.ngbDateParserFormatter.format(copyItem.deploymentDate).replace(/-/g, '/');
    }
    // P終了日
    if (copyItem.pendDate && typeof copyItem.pendDate !== 'string') {
      copyItem.pendDate = this.ngbDateParserFormatter.format(copyItem.pendDate).replace(/-/g, '/');
    }
    return copyItem;
  }

  /**
   * ブランドコードとアイテムコードを基にJSONから加算日を取得する
   * @param brandCode ブランドコード
   * @param itemCode アイテムコード
   * @returns 加算日
   */
  getAddDays(brandCode: string, itemCode: string): number {
    let addDay = 0; // 加算日

    CodeMaster.addDays.some(addDays => {
      if (brandCode === addDays.brandCode && itemCode === addDays.itemCode) {
        addDay = addDays.addDay;
        console.debug('ブランドコードとアイテムコードに合致する加算日＝', addDay);
        return true;
      } else {
        // ブランドコードとアイテムコードに合致するものがない場合、デフォルトの加算日を返す
        if (addDays.brandCode === null && addDays.itemCode === null) {
          addDay = addDays.addDay;
          console.debug('デフォルトの加算日＝', addDay);
          return true;
        }
      }
    });

    console.debug('加算日：', addDay);
    return addDay;
  }

  /**
   * コードマスタのリストから初期値フラグが設定されているIndexを取得する。
   * @pram codmstlist コードマスタのリスト
   * @return index
   */
  getInitIndex(codmstlist: JunpcCodmst[]): number {
    if (ListUtils.isEmpty(codmstlist)) { return null; }

    let initalIndex = null;

    codmstlist.some((item, index) => {
      if (item.item30 === Const.M_CODMST_INITIAL_FLAG) {
        initalIndex = index;
        return true;
      }
    });

    return initalIndex;
  }

  /**
   * SKU(色・コード)バリデーション.
   * @param formValue Form入力値
   * @param viewMode 画面表示モード
   * @return エラーメッセージリスト
   */
  validSkus(formValue: any, viewMode: number): string[] {
    const skus: SkuFormValue[] = formValue.skus;
    const errMsg: string[] = [];

    let existsOnlySizeNoData = false;  // 片方しか入力がないSKUが1件でも存在するか
    let existsBothData = false;        // 両方入力されたSKUが1件も存在するか

    // カラーコード別にチェック
    const eniqueErrorColorCode: string[] = [];
    skus.forEach((sku1, idx1, array) => {
      const colorCode = sku1.colorCode;
      const selectedSizeList = sku1.sizeList.filter(size => size.select);

      // カラー重複エラー
      array.forEach((sku2, idx2) => {
        if (idx1 !== idx2 && colorCode === sku2.colorCode
          && (StringUtils.isNotEmpty(colorCode) || StringUtils.isNotEmpty(sku2.colorCode))
          && eniqueErrorColorCode.findIndex(eniqueCode => eniqueCode === colorCode) === -1) {
          eniqueErrorColorCode.push(colorCode);
          errMsg.push('<nobr>色【' + colorCode + '】' + sku1.colorName + '&nbsp;&nbsp;</nobr>' + 'ERRORS.VALIDATE.SKUS_COLOR_EXIST');
        }
      });

      if (FormUtils.isNotEmpty(colorCode)) {
        if (ListUtils.isEmpty(selectedSizeList)) {
          // 色を指定しているが、サイズが入力なしの場合
          existsOnlySizeNoData = true;
        } else {
          // 色、サイズともに入力ある
          existsBothData = true;
        }
      }
    });

    // 片方だけ入力が1件でもあればエラー
    if (existsOnlySizeNoData) { errMsg.push('ERRORS.VALIDATE.SKUS_NOT_MATCH'); }
    // 両方入力が1件もなければエラー。ただし品番更新時のみチェックする
    if (viewMode === ViewMode.PART_EDIT && !existsBothData) { errMsg.push('ERRORS.VALIDATE.SKUS_NO_DATA'); }
    return errMsg;
  }
}
