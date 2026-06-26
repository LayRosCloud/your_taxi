package com.leafall.yourtaxi.core.db;

import com.leafall.yourtaxi.entity.CodeEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.repository.CodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.leafall.yourtaxi.core.utils.FakerUtils.faker;

@Component
@RequiredArgsConstructor
public class CodeDbHelper {
    private final CodeRepository codeRepository;
    private final UserDbHelper userDbHelper;

    public CodeEntity save(UserEntity user) {
        var code = new CodeEntity();
        code.setCode(faker.number().digits(6));
        code.setUser(user);
        return codeRepository.save(code);
    }

    public CodeEntity save() {
        var code = new CodeEntity();
        code.setCode(faker.number().digits(6));
        code.setUser(userDbHelper.save());
        return codeRepository.save(code);
    }
}
