import { FormGroup, AbstractControl, FormArray } from '@angular/forms';
import { Distribute } from './distribute';

/**
 * 新規登録画面配分処理クラス.
 */
export class NewDistribute extends Distribute {
  filterTargetDeliveryStores(mainForm: FormGroup): AbstractControl[] {
    return (<FormArray> mainForm.get('deliveryStores')).controls;
  }
}
