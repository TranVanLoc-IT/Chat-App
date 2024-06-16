package chat.app.chatapp.model;

public class HandleData {
    
    public static String[] HandleNameField(String name){
        String[] comps = new String[3];
        String[] nameStrucs = name.split(" ");
        if(nameStrucs.length <= 3)
        {
            return nameStrucs;
        }
        else{
            comps[0] = nameStrucs[0];
            comps[2] = nameStrucs[nameStrucs.length - 1];
            comps[1] = "";
            for(int i = 1; i < nameStrucs.length - 1; i++)
            {
                comps[2] += nameStrucs[i] + ' ';
            }
        }
        return comps;
    }
}
