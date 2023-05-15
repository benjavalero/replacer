import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { PublicIp } from '../../api/models/public-ip';
import { AdministrationApiService } from '../../api/services/administration-api.service';

@Component({
  standalone: true,
  selector: 'app-public-ip',
  imports: [CommonModule],
  templateUrl: './public-ip.component.html',
  styleUrls: []
})
export class PublicIpComponent implements OnInit {
  publicIp$!: Observable<PublicIp>;

  constructor(private administrationApiService: AdministrationApiService) {}

  ngOnInit(): void {
    this.publicIp$ = this.administrationApiService.getPublicIp();
  }
}
