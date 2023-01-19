import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { PublicIp } from '../../api/models/public-ip';
import { AdministrationService } from '../../api/services/administration.service';

@Component({
  selector: 'app-public-ip',
  templateUrl: './public-ip.component.html',
  styleUrls: []
})
export class PublicIpComponent implements OnInit {
  publicIp$!: Observable<PublicIp>;

  constructor(private administrationService: AdministrationService) {}

  ngOnInit(): void {
    this.publicIp$ = this.administrationService.getPublicIp();
  }
}
