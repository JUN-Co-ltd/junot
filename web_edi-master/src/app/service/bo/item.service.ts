import { Injectable } from '@angular/core';
import { ListUtils } from 'src/app/util/list-utils';
import { Compositions } from 'src/app/model/compositions';
import { QualityApprovalStatus, CompositionsCommon } from 'src/app/const/const';
import { Item } from 'src/app/model/item';
import { AbstractControl } from '@angular/forms';
import { Regex } from 'src/app/const/regex';

@Injectable({
  providedIn: 'root'
})
export class ItemService {

  constructor() { }

  /**
   * @param item 品番情報
   * @returns true: 共通の組成を非活性にする
   */
  isDisableCommonComposition(item: Item): boolean {
    return item.qualityCompositionStatus === QualityApprovalStatus.ACCEPT
      || this.existsUsingCommonCompositionApprovedColor(item.approvedColors, item.compositions);
  }

  /**
   * @param approvedColors 承認済みのカラーリスト
   * @param compositions t_compositionの組成リスト
   * @returns true:優良誤認承認済みカラーの中で共通の組成を使っているカラーあり
   */
  private existsUsingCommonCompositionApprovedColor(approvedColors: string[], compositions: Compositions[]): boolean {
    if (ListUtils.isEmpty(approvedColors)) { return false; }

    return approvedColors.some(ac => !compositions.some(c => c.colorCode === ac));
  }

  /**
   * @param approvedColors　優良誤認承認済みカラーリスト
   * @param f AbstractControl
   * @return true: 優良誤認承認済カラーのフォーム
   */
  isApprovedColorForm = (approvedColors: string[]) => (f: AbstractControl): boolean => {
    if (ListUtils.isEmpty(approvedColors)) {
      return false;
    }

    return approvedColors.some(ac => ac === f.get('colorCode').value);
  }

  /**
   * @param productCode 商品コード
   * @return 分割した商品コード
   */
  splitProductCode(productCode: string): { partNo: string, colorCode: string, size: string } {
    const nullValue = { partNo: null, colorCode: null, size: null };

    if (productCode == null) {
      return nullValue;
    }

    const strList = productCode.split(Regex.HYPHEN);

    // XXXYYYYY-XX-XX
    if (strList.length === 3) {
      return {
        partNo: strList[0],
        colorCode: strList[1],
        size: strList[2]
      };
    }

    // XXX-YYYYY-XX-XX
    if (strList.length === 4) {
      return {
        partNo: strList[0] + strList[1],
        colorCode: strList[2],
        size: strList[3]
      };
    }

    return nullValue;
  }

  /**
   * @param f AbstractControl
   * @return true: 共通の組成のフォーム
   */
  isCommonCompositionForm = (f: AbstractControl): boolean => CompositionsCommon.COLOR_CODE === f.get('colorCode').value;
}
