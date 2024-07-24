package cz.iocb.chemweb.client.widgets.filter;



public interface IFilter<Type>
{
    boolean isValid(Type value);
}
