@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  login(loginRequest: LoginRequest) {
    return this.http.post(
      `${this.apiUrl}/auth/login`,
      loginRequest
    );
  }
}