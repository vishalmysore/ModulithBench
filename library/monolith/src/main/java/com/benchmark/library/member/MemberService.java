package com.benchmark.library.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member createMember(Member member) {
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Member> getActiveMembers() {
        return memberRepository.findByActive(true);
    }

    @Transactional(readOnly = true)
    public List<Member> searchByName(String name) {
        return memberRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
    }

    @Transactional
    public Member updateMember(Long id, Member memberDetails) {
        Member member = getMemberById(id);
        member.setFirstName(memberDetails.getFirstName());
        member.setLastName(memberDetails.getLastName());
        member.setEmail(memberDetails.getEmail());
        member.setPhone(memberDetails.getPhone());
        member.setAddress(memberDetails.getAddress());
        member.setMaxBooksAllowed(memberDetails.getMaxBooksAllowed());
        return memberRepository.save(member);
    }

    @Transactional
    public Member deactivateMember(Long id) {
        Member member = getMemberById(id);
        member.setActive(false);
        return memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(Long id) {
        Member member = getMemberById(id);
        memberRepository.delete(member);
    }

    // Called by LoanService — validates member is active before allowing a loan
    @Transactional(readOnly = true)
    public void validateActiveMember(Long memberId) {
        Member member = getMemberById(memberId);
        if (!member.isActive()) {
            throw new IllegalStateException("Member " + memberId + " is not active and cannot borrow books");
        }
    }

    // Called by LoanService — checks member hasn't exceeded borrow limit
    @Transactional(readOnly = true)
    public int getMaxBooksAllowed(Long memberId) {
        return getMemberById(memberId).getMaxBooksAllowed();
    }
}
