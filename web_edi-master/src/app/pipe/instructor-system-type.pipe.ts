import { Pipe, PipeTransform } from '@angular/core';
import { InstructorSystemType } from 'src/app/const/const';

@Pipe({
  name: 'instructorSystemType'
})
export class InstructorSystemTypePipe implements PipeTransform {
  /**
   * 指示元システムを文字列にして返す
   * @param instructorSystemType 指示元システム
   */
  transform(instructorSystemType: InstructorSystemType): string {
    switch (instructorSystemType) {
      case InstructorSystemType.JADORE:
        return 'JADORE';
      case InstructorSystemType.ZOZO:
        return 'ZOZO';
      case InstructorSystemType.SCS:
        return 'SCS';
      case InstructorSystemType.SV:
        return 'SV';
      case InstructorSystemType.ARO:
        return 'ARO';
    }
  }
}
