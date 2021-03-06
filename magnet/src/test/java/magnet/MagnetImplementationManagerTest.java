package magnet;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import magnet.internal.Factory;
import magnet.internal.Range;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MagnetImplementationManagerTest {

    @Mock
    Factory<Type1> factoryType1Impl1;

    @Mock
    Factory<Type1> factoryType1Impl2;

    @Mock
    Factory<Type2> factoryType2Impl1;

    @Mock
    Factory<Type2> factoryType2Impl2;

    @Mock
    DependencyScope dependencyScope;

    private MagnetImplementationManager registry;

    @Before
    public void before() {
        registry = new MagnetImplementationManager();

        when(factoryType1Impl1.create(any())).thenReturn(new Type1Impl());
        when(factoryType1Impl2.create(any())).thenReturn(new Type1Impl());
        when(factoryType2Impl1.create(any())).thenReturn(new Type2Impl());
        when(factoryType2Impl2.create(any())).thenReturn(new Type2Impl());

        Factory[] factories = new Factory[] {
                factoryType1Impl1,
                factoryType1Impl2,
                factoryType2Impl1,
                factoryType2Impl2
        };

        Map<Class, Object> index = new HashMap<>();

        Map<String, Range> ranges1 = new HashMap<>();
        ranges1.put("impl1", new Range(0, 1, "impl1"));
        ranges1.put("impl2", new Range(1, 1, "impl2"));

        index.put(Type1.class, ranges1);
        index.put(Type2.class, new Range(2, 2, ""));

        registry.register(factories, index);
    }

    @Test
    public void test_GetMany_UnknownType_NoTarget() {
        // when
        List<Object> impls = registry.getMany(Object.class, dependencyScope);

        // then
        assertThat(impls).isEmpty();
    }

    @Test
    public void test_GetMany_Type_Target_default() {
        // when
        List<Type2> impls = registry.getMany(Type2.class, dependencyScope);

        // then
        verify(factoryType2Impl1).create(dependencyScope);
        verify(factoryType2Impl2).create(dependencyScope);
        assertThat(impls).hasSize(2);
        assertThat(impls.get(0)).isNotNull();
        assertThat(impls.get(1)).isNotNull();
    }

    @Test
    public void test_GetMany_Type_Target_impl1() {
        // when
        List<Type1> impls = registry.getMany(Type1.class, "impl1", dependencyScope);

        // then
        verify(factoryType1Impl1).create(dependencyScope);
        assertThat(impls).hasSize(1);
        assertThat(impls.get(0)).isNotNull();
    }

    @Test
    public void test_GetMany_Type_Target_impl2() {
        // when
        List<Type1> impls = registry.getMany(Type1.class, "impl2", dependencyScope);

        // then
        verify(factoryType1Impl2).create(dependencyScope);
        assertThat(impls).hasSize(1);
        assertThat(impls.get(0)).isNotNull();
    }

    @Test
    public void test_GetSingle_OneFound() {
        // when
        Type1 impl = registry.getSingle(Type1.class, "impl2", dependencyScope);

        // then
        assertThat(impl).isNotNull();
        verify(factoryType1Impl2).create(dependencyScope);
    }

    @Test
    public void test_GetSingle_NoneFound() {
        // when
        Type3 impl = registry.getSingle(Type3.class, dependencyScope);

        // then
        assertThat(impl).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void test_GetSingle_MultipleFound() {
        registry.getSingle(Type2.class, dependencyScope);
    }

    @Test
    public void test_RequireSingle_OneFound() {
        // when
        Type1 impl = registry.requireSingle(Type1.class, "impl2", dependencyScope);

        // then
        assertThat(impl).isNotNull();
        verify(factoryType1Impl2).create(dependencyScope);
    }

    @Test(expected = IllegalStateException.class)
    public void test_RequireSingle_NoneFound() {
        registry.requireSingle(Type3.class, dependencyScope);
    }

    @Test(expected = IllegalStateException.class)
    public void test_RequireSingle_MultipleFound() {
        registry.requireSingle(Type2.class, dependencyScope);
    }

    interface Type1 {}

    interface Type2 {}

    interface Type3 {}

    class Type1Impl implements Type1 {}

    class Type2Impl implements Type2 {}

}
