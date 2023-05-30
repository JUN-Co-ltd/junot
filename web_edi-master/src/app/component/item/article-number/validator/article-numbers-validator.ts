import { ValidationErrors, AsyncValidatorFn, FormArray } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

import { ItemService } from 'src/app/service/item.service';

import { Item } from 'src/app/model/item';
import { Sku } from 'src/app/model/sku';

import { FormUtils } from 'src/app/util/form-utils';
import { ListUtils } from 'src/app/util/list-utils';

/**
 * JAN/UPCフォームのAPIチェックバリデーション.
 * ・JAN/UPCコード全て未入力の場合は、チェックしない
 * ・未入力のJAN/UPCコードは、チェックしない
 * ・JAN/UPCコード全てDB登録値と同じ場合(未登録のものは空)は、チェックしない
 * ・含まれるAPIチェックエラー：
 *       -社内JAN枠範囲内エラー(400_I_18)
 *       -登録済エラー(400_I_19)
 *       -重複エラー(400_I_20)
 *       -チェックデジットエラー(400_I_21)
 * @returns Promise<ValidationErrors | null> | Observable<ValidationErrors | null>
 */
export function articleNumbersValidator(
  itemService: ItemService,
  janType: number
): AsyncValidatorFn {
  return (formArray: FormArray): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> => {
    const skus: Sku[] = [];
    const registedJanCodeSet = new Set<String>();

    formArray.controls.forEach(color => {
      (<FormArray> color.get('sizeList')).controls.forEach(size => {
        const id = size.get('id').value as number;
        const janCode = size.get('janCode').value as string;
        const registedJanCode = size.get('registedJanCode').value as string;

        // 値があるJANコードのSKUを取得(重複あり)
        if (FormUtils.isNotEmpty(janCode)) {
          skus.push({
            id: id,
            janCode: janCode
          } as Sku
          );
        }

        // JANコードのDB登録値Setを取得(重複なし)
        if (FormUtils.isNotEmpty(registedJanCode)) {
          registedJanCodeSet.add(registedJanCode);
        }
      });
    });

    if (ListUtils.isEmpty(skus)) {
      // 全ての活性のJAN/UPCコード入力欄に入力がない場合、処理を終了
      return of(null);
    }

    // DBに登録されていないJANコードがあるか
    const isUnregisteredJanCodeExist = skus.some(sku => !registedJanCodeSet.has(sku.janCode));
    // 各登録済のJANコードを複数回入力しているJANコードがあるか(重複存在チェック)
    const isInputPluralRegistedJanCode = (Array.from(registedJanCodeSet)).some(registedJanCode => {
      return skus.filter(sku => sku.janCode === registedJanCode).length > 1;
    });

    if (!isUnregisteredJanCodeExist && !isInputPluralRegistedJanCode) {
      // 全てのJANコードがDB登録値と同じ(未登録のものは空) かつ 登録済のJANコードと重複なし の場合、処理を終了(初期表示時など)
    return of(null);
    }

    // APIバリデーションチェック
    return itemService.validArticleNumber({
      janType: janType,
      skus: skus
    } as Item).pipe(
      map((validate) => {
        if (ListUtils.isNotEmpty(validate.errors)) {
          return { validate: validate.errors};
        }
        return null;
      })
    );
  };
}
