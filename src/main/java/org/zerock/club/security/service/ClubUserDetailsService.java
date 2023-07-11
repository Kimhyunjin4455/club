package org.zerock.club.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.zerock.club.entity.ClubMember;
import org.zerock.club.repository.ClubMemberRepository;
import org.zerock.club.security.dto.ClubAuthMemberDTO;

import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ClubUserDetailsService implements UserDetailsService {

    // ClubMemberRepository를 주입받을 수 있는 구조로 변경하고 @RequiredArgsConstructor 처리
    private final ClubMemberRepository clubMemberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        log.info("ClubUserDetailsService loadUserByUsername " + username);

        // username이 실제로는 ClubMember에서는 email을 의미 > ClubMemberRepository의 findByEmail() 호출 / social 여부 false
        Optional<ClubMember> result = clubMemberRepository.findByEmail(username, false);
        // 사용자 존재 x > UsernameNotFoundException 처리
        if(result.isEmpty()){
            throw new UsernameNotFoundException("Check Email or Social");
        }
        ClubMember clubMember = result.get();

        log.info("-----------------");
        log.info(clubMember);

        // ClubMember를 UserDetails 타입으로 처리하기 위해 ClubAuthMemberDTO 타입으로 변환
        ClubAuthMemberDTO clubAuthMember = new ClubAuthMemberDTO(
                clubMember.getEmail(),
                clubMember.getPassword(),
                clubMember.isFromSocial(),
                clubMember.getRoleSet().stream().map(role -> new SimpleGrantedAuthority
                        ("ROLE_" + role.name())).collect(Collectors.toSet())
        ); // ClubMemberRole은 스프링 시큐리티에서 사용하는 SimpleGrantedAuthority로 변환. 이떄 'ROLE_'라는 접두어 추가해서 사용

        clubAuthMember.setName(clubMember.getName());
        clubAuthMember.setFromSocial(clubMember.isFromSocial());

        return clubAuthMember;

    }
}
