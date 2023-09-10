package com.example.chang.auth.security;


import com.example.chang.common.exceptions.CustomException;
import com.example.chang.common.exceptions.Error;
import com.example.chang.member.entity.Member;
import com.example.chang.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (memberRepository.findByEmail(email).isPresent()){
            Member findMember = (Member) memberRepository.findByEmail(email).orElseThrow(
                    () -> new CustomException(Error.NOT_EXIST_USER));
            return new UserDetailsImpl(findMember, findMember.getEmail());
        }
        Optional<Member> findMember = memberRepository.findById(Long.valueOf(email));
        return new UserDetailsImpl(findMember.get(), findMember.get().getEmail());
    }
}
