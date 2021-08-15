package web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import web.model.Role;
import web.model.User;
import web.repository.RoleRepository;
import web.repository.UserRepository;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class AppServiceImpl implements AppService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AppServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUser(Long userId) {
        return userRepository.find(userId)
                .orElseThrow(() -> new EmptyResultDataAccessException(String
                        .format("User с ID = %d не найден", userId), 1));
    }

    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.find(email);
        if (null == user) {
            throw new UsernameNotFoundException(String.format("User с email %s не найден", email));
        }
        return user;
    }

    @Override
    public void deleteUser(Long userId) {
        Optional<User> user = userRepository.find(userId);
        if (user.isPresent()) {
            try {
                userRepository.delete(user.get());
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public boolean saveUser(User user, BindingResult bindingResult, Model model) {
        model.addAttribute("allRoles", findAllRoles());

        if (bindingResult.hasErrors()) {
            return false;
        }

        for (Role role : user.getRoles()) {
            try {
                role.setId(roleRepository.findRoleByAuthority(role.getAuthority()).getId());
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            userRepository.save(user);
        } catch (PersistenceException e) { // org.hibernate.exception.ConstraintViolationException
            model.addAttribute("persistenceException", true);
            return false;
        }

        return true;
    }
}
