import { Injectable } from '@angular/core';
import { MakerReturnProductSearchConditions } from '../model/maker-return-product-search-conditions';
import { JunotApiService } from './junot-api.service';
import { Observable } from 'rxjs';
import { GenericList } from '../model/generic-list';
import { MakerReturnProductComposite } from '../model/maker-return-product-composite';
import { StringUtilsService } from './bo/string-utils.service';
import { ItemService } from './bo/item.service';

const BASE_URL = '/makerReturnProducts';

@Injectable({
  providedIn: 'root'
})
export class MakerReturnProductsHttpService {

  constructor(
    private junotApiService: JunotApiService,
    private stringUitls: StringUtilsService,
    private itemService: ItemService
  ) { }

  /**
   * 検索処理.
   * @param formValue 検索フォーム値
   * @returns レスポンス
   */
  search(formValue: MakerReturnProductSearchConditions): Observable<GenericList<MakerReturnProductComposite>> {
    const searchCondition = this.formatSearchCondition(formValue);
    return this.junotApiService.listByPost(`${ BASE_URL }/search`, searchCondition);
  }

  /**
   * @param formValue 検索フォーム値
   * @returns フォーマットした検索条件
   */
  private formatSearchCondition(formValue: MakerReturnProductSearchConditions): MakerReturnProductSearchConditions {
    const copy = Object.assign({}, { ...formValue }); // deep copy

    // stringをスペース区切りでリスト化
    const brandCodes = this.stringUitls.splitByWhitespace(formValue.brandCodes as string);
    const itemCodes = this.stringUitls.splitByWhitespace(formValue.itemCodes as string);
    const colorCodes = this.stringUitls.splitByWhitespace(formValue.colorCodes as string);
    const sizeList = this.stringUitls.splitByWhitespace(formValue.sizeList as string);

    // 品番ハイフン除去
    const partNo = this.stringUitls.deleteHyphen(formValue.partNo);

    // 商品コード分割
    const splitProductCode = this.itemService.splitProductCode(copy.productCode);

    return Object.assign({ ...copy }, {
      brandCodes,
      itemCodes,
      colorCodes,
      sizeList,
      partNo: partNo === '' ? null : partNo,
      partNoOfProductCode: splitProductCode.partNo,
      colorCodeOfProductCode: splitProductCode.colorCode,
      sizeOfProductCode: splitProductCode.size
    });
  }
}
