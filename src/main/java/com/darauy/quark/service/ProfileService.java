package com.darauy.quark.service;

import com.darauy.quark.entity.users.Profile;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.repository.ProfileRepository;
import com.darauy.quark.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void uploadProfilePicture(Integer userId, MultipartFile image) throws Exception {
        if (image == null || image.isEmpty()) throw new IllegalArgumentException("No file provided");
        if (image.getSize() > 2 * 1024 * 1024) throw new IllegalArgumentException("File too large");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Profile profile = profileRepository.findById(userId)
                .orElse(Profile.builder().user(user).build());

        profile.setImage(image.getBytes());
        profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public String fetchProfilePictureBase64(Integer userId) throws Exception {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (profile.getImage() == null) return null;

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(profile.getImage());
    }

    @Transactional
    public void clearProfilePicture(Integer userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        profile.setImage(null);
        profileRepository.save(profile);
    }

    @Transactional
    public void updateBio(Integer userId, String bio) {
        if (bio != null && bio.length() > 255) throw new IllegalArgumentException("Bio too long");

        Profile profile = profileRepository.findById(userId)
                .orElse(Profile.builder().user(userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found")))
                        .build());

        profile.setBio(bio);
        profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public String fetchBio(Integer userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return profile.getBio() == null ? "" : profile.getBio();
    }
}
