import java.util.Objects;

public class Package
{
    private String name;
    private String license;
    private String version;
    private boolean isDevDep;

    public Package () //Empty Constructor
    {
        this.license = "";
        this.version = "";
        this.name = "";
        this.isDevDep = false;
    }

    public Package (String name, String version, boolean isDevDep) //Primary constructor to be used in Grabber
    {
        this.license = "";
        this.version = version;
        this.name = name;
        this.isDevDep = isDevDep;
    }

    public Package (String name, String license, String version, boolean isDevDep) //Full Constructor
    {
        this.license = license;
        this.version = version;
        this.name = name;
        this.isDevDep = isDevDep;
    }

    public void setLicense (String license)
    {
        this.license = license;
    }

    public void setVersion (String version)
    {
        this.version = version;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public void setDevDep (boolean devDep)
    {
        isDevDep = devDep;
    }

    public String getLicense ()
    {
        return license;
    }

    public String getVersion ()
    {
        return version;
    }

    public String getName ()
    {
        return name;
    }

    public boolean isDevDep ()
    {
        return isDevDep;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Package aPackage = (Package) o;
        if (aPackage.getName().equals(this.name))
            return (true);
        return (false);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }

    @Override
    public String toString()
    {
        return "Package{" +
                "license='" + license + '\'' +
                ", version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", isDevDep='" + isDevDep + '\'' +
                '}';
    }
}
