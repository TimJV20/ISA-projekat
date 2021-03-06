package JV20.isapsw.service;

import JV20.isapsw.model.Korisnik;
import JV20.isapsw.repository.KorisnikRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    protected final Log LOGGER = LogFactory.getLog(getClass());

    @Autowired
    private KorisnikRepository korisnikRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Korisnik user = korisnikRepository.findByKorisnickoIme(username);
        if(user==null){
            throw new UsernameNotFoundException(String.format("Ne postoji korisnik sa tim nazivom!"));
        } else {
            return user;
        }
    }

    // Funkcija pomocu koje korisnik menja svoju lozinku
    public void changePassword(String oldPassword, String newPassword) {

        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        String username = currentUser.getName();

        if (authenticationManager != null) {
            LOGGER.debug("Re-authenticating user '" + username + "' for password change request.");

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, oldPassword));
        } else {
            LOGGER.debug("No authentication manager set. can't change Password!");

            return;
        }

        LOGGER.debug("Changing password for user '" + username + "'");

        Korisnik user = (Korisnik) loadUserByUsername(username);

        // pre nego sto u bazu upisemo novu lozinku, potrebno ju je hesirati
        // ne zelimo da u bazi cuvamo lozinke u plain text formatu
        user.setLozinka(passwordEncoder.encode(newPassword));
        korisnikRepository.save(user);

    }

    // Funkcija pomocu koje korisnik menja svoju lozinku
    public Korisnik changePasswordFirstTime(String newPassword) {

        Korisnik user = (Korisnik) loadUserByUsername((SecurityContextHolder.getContext().getAuthentication().getName()));
        user.setLozinka(passwordEncoder.encode(newPassword));
        user.setLastPasswordResetDate(new Timestamp(new Date().getTime()));
        user.setPromijenjenaLozinka(true);
        return korisnikRepository.save(user);

    }
}
