public class Member extends User {
    private String email;
    private String address;
    private String phone;
    private String membershipDate;

    public Member(int id, String name, String Address, String Phone, String Email,String MembershipDate) {
        super(id, name);
        this.address = Address;
        this.email = Email;
        this.phone = Phone;
        this.membershipDate = MembershipDate;
    }
    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getMembershipDate() {
        return membershipDate;
    }
}