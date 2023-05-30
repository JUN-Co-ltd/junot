import { Component } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { LoadingService } from 'src/app/service/loading.service';

@Component({
  selector: 'app-loading',
  templateUrl: './loading.component.html',
  styleUrls: ['./loading.component.scss']
})
export class LoadingComponent {

  constructor(
    private loadingService: LoadingService
  ) { }

  get isLoading(): BehaviorSubject<boolean> {
    return this.loadingService.isLoading$;
  }
}
