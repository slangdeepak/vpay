package in.slanglabs.vpay.model;

public class Contact {
    public String name;
    public String upiId;

    public Contact(String name, String upiId){
        this.name = name;
        this.upiId = upiId;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Contact && ((Contact) obj).name.equalsIgnoreCase(this.name));

    }

    @Override
    public String toString() {
        return "Contact : " + name + ":" + upiId;
    }
}
