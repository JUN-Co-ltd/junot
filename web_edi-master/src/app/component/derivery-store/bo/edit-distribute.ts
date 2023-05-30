

import { FormGroup, AbstractControl, FormArray } from '@angular/forms';
import { Distribute } from './distribute';

/**
 * 編集画面配分処理クラス.
 */
export class EditDistribute extends Distribute {
  filterTargetDeliveryStores(mainForm: FormGroup, divisionCode: string): AbstractControl[] {
    const fCtrlDeliveryStores: AbstractControl[] = (<FormArray> mainForm.get('deliveryStores')).controls;
    return fCtrlDeliveryStores.filter(ds => ds.get('divisionCode').value === divisionCode);
  }
}
