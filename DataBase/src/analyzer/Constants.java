package analyzer;

public interface Constants extends ScannerConstants, ParserConstants
{
    int EPSILON  = 0;
    int DOLLAR   = 1;

    int t_identificador = 2;
    int t_constInteger = 3;
    int t_constFloat = 4;
    int t_constLiteral = 5;
    int t_data = 6;
    int t_database = 7;
    int t_table = 8;
    int t_colunm = 9;
    int t_create = 10;
    int t_set = 11;
    int t_describe = 12;
    int t_insert = 13;
    int t_into = 14;
    int t_values = 15;
    int t_update = 16;
    int t_delete = 17;
    int t_alter = 18;
    int t_add = 19;
    int t_drop = 20;
    int t_select = 21;
    int t_from = 22;
    int t_where = 23;
    int t_order = 24;
    int t_by = 25;
    int t_integer = 26;
    int t_float = 27;
    int t_varchar = 28;
    int t_date = 29;
    int t_constraint = 30;
    int t_null = 31;
    int t_not = 32;
    int t_unique = 33;
    int t_primary = 34;
    int t_key = 35;
    int t_foreign = 36;
    int t_references = 37;
    int t_and = 38;
    int t_or = 39;
    int t_asc = 40;
    int t_desc = 41;
    int t_TOKEN_42 = 42; //"*"
    int t_TOKEN_43 = 43; //"="
    int t_TOKEN_44 = 44; //"<"
    int t_TOKEN_45 = 45; //"<="
    int t_TOKEN_46 = 46; //">"
    int t_TOKEN_47 = 47; //">="
    int t_TOKEN_48 = 48; //"<>"
    int t_TOKEN_49 = 49; //"."
    int t_TOKEN_50 = 50; //","
    int t_TOKEN_51 = 51; //";"
    int t_TOKEN_52 = 52; //"("
    int t_TOKEN_53 = 53; //")"

}
